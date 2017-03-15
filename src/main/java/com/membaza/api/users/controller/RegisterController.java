package com.membaza.api.users.controller;

import com.membaza.api.users.dto.PasswordDto;
import com.membaza.api.users.dto.UserDto;
import com.membaza.api.users.event.RegistrationCompleteEvent;
import com.membaza.api.users.handler.GenericResponse;
import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.persistence.model.VerificationToken;
import com.membaza.api.users.service.captcha.CaptchaService;
import com.membaza.api.users.service.user.UserSecurityService;
import com.membaza.api.users.service.user.UserService;
import com.membaza.api.users.throwable.InvalidOldPasswordException;
import com.membaza.api.users.throwable.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Controller
@RequestMapping("/api/v1")
public final class RegisterController {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(RegisterController.class);

    private final UserService userService;
    private final CaptchaService captchaService;
    private final MessageSource messages;
    private final JavaMailSender mailSender;
    private final ApplicationEventPublisher eventPublisher;
    private final UserSecurityService userSecurityService;
    private final Environment env;

    public RegisterController(
            final UserService userService,
            final CaptchaService captchaService,
            final MessageSource messages,
            final JavaMailSender mailSender,
            final ApplicationEventPublisher eventPublisher,
            final UserSecurityService userSecurityService,
            final Environment env) {

        this.userService         = requireNonNull(userService);
        this.captchaService      = requireNonNull(captchaService);
        this.messages            = requireNonNull(messages);
        this.mailSender          = requireNonNull(mailSender);
        this.eventPublisher      = requireNonNull(eventPublisher);
        this.userSecurityService = requireNonNull(userSecurityService);
        this.env                 = requireNonNull(env);
    }

    @ResponseBody
    @PostMapping("/users/register")
    public GenericResponse registerUserAccount(
            final @Valid UserDto accountDto,
            final HttpServletRequest request) {

        final String response = request.getParameter("g-recaptcha-response");
        captchaService.processResponse(response);

        LOGGER.debug("Registering user account with information: {}", accountDto);

        final User registered = userService.registerNewUserAccount(accountDto);
        eventPublisher.publishEvent(new RegistrationCompleteEvent(
            registered, request.getLocale(), getAppUrl(request)));

        return new GenericResponse("success");
    }

    @GetMapping("/users/register/confirm")
    public String confirmRegistration(
            final Locale locale,
            final Model model,
            final @RequestParam("token") String token
    ) throws UnsupportedEncodingException {

        final String result = userService.validateVerificationToken(token);

        if (result.equals("valid")) {
            final User user = userService.getUser(token);

            if (user.isUsing2FA()) {
                model.addAttribute("qr", userService.generateQRUrl(user));
                return "redirect:/qrcode.html?lang=" + locale.getLanguage();
            }

            model.addAttribute("message", messages.getMessage(
                "message.accountVerified", null, locale));

            return "redirect:/login?lang=" + locale.getLanguage();
        }

        model.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
        model.addAttribute("expired", "expired".equals(result));
        model.addAttribute("token", token);

        return "redirect:/badUser.html?lang=" + locale.getLanguage();
    }

    @ResponseBody
    @GetMapping("/user/register/resend")
    public GenericResponse resendRegistrationToken(
            final HttpServletRequest request,
            final @RequestParam("token") String existingToken) {

        final VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        final User user = userService.getUser(newToken.getToken());

        mailSender.send(constructResendVerificationTokenEmail(
            getAppUrl(request), request.getLocale(), newToken, user));

        return new GenericResponse(messages.getMessage(
            "message.resendToken", null, request.getLocale()
        ));
    }

    @ResponseBody
    @PostMapping("/user/password/reset")
    public GenericResponse resetPassword(
            final HttpServletRequest request,
            final @RequestParam("email") String userEmail) {

        final User user = userService.findUserByEmail(userEmail);
        if (user == null) {
            throw new UserNotFoundException();
        }

        final String token = UUID.randomUUID().toString();

        userService.createPasswordResetTokenForUser(user, token);
        mailSender.send(constructResetTokenEmail(
            getAppUrl(request), request.getLocale(), token, user)
        );

        return new GenericResponse(messages.getMessage(
            "message.resetPasswordEmail", null, request.getLocale()
        ));
    }

    @GetMapping("/user/password/change")
    public String showChangePasswordPage(
            final Locale locale,
            final Model model,
            final @RequestParam("id") long id,
            final @RequestParam("token") String token) {

        final String result = userSecurityService.validatePasswordResetToken(id, token);
        if (result != null) {
            model.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
            return "redirect:/login?lang=" + locale.getLanguage();
        }

        return "redirect:/updatePassword.html?lang=" + locale.getLanguage();
    }

    @ResponseBody
    @PostMapping("/user/password/save")
    public GenericResponse savePassword(
            final Locale locale,
            final @Valid PasswordDto passwordDto) {

        final User user = (User) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        userService.changeUserPassword(user, passwordDto.getNewPassword());
        return new GenericResponse(messages.getMessage(
            "message.resetPasswordSuc", null, locale));
    }

    // change user password
    @ResponseBody
    @PostMapping("/user/password/update")
    public GenericResponse changeUserPassword(
            final Locale locale,
            final @Valid PasswordDto passwordDto) {

        final User user = userService.findUserByEmail(
            ((User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
            ).getEmail());

        if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
            throw new InvalidOldPasswordException();
        }

        userService.changeUserPassword(user, passwordDto.getNewPassword());
        return new GenericResponse(messages.getMessage("message.updatePasswordSuc", null, locale));
    }

    @ResponseBody
    @PostMapping("/user/update/2fa")
    public GenericResponse modifyUser2FA(
            final @RequestParam("use2FA") boolean use2FA
    ) throws UnsupportedEncodingException {

        final User user = userService.updateUser2FA(use2FA);
        if (use2FA) {
            return new GenericResponse(userService.generateQRUrl(user));
        }

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //                             Internal Methods                           //
    ////////////////////////////////////////////////////////////////////////////

    private SimpleMailMessage constructResendVerificationTokenEmail(
            final String contextPath,
            final Locale locale,
            final VerificationToken newToken,
            final User user) {

        final String confirmationUrl = contextPath +
            "/registrationConfirm.html?token=" + newToken.getToken();

        final String message = messages.getMessage("message.resendToken", null, locale);
        return constructEmail("Resend Registration Token", message + " \r\n" + confirmationUrl, user);
    }

    private SimpleMailMessage constructResetTokenEmail(
            final String contextPath,
            final Locale locale,
            final String token,
            final User user) {

        final String url = contextPath + "/user/changePassword?id=" + user.getId() + "&token=" + token;
        final String message = messages.getMessage("message.resetPassword", null, locale);
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(
            final String subject,
            final String body,
            final User user) {

        final SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(env.getProperty("support.email"));
        return email;
    }

    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" +
            request.getServerPort() + request.getContextPath();
    }
}