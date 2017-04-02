package com.membaza.api.users.service.text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service
public final class TextServiceImpl implements TextService {

    // TODO: Use Guava to cache generated messages

    private static final String MESSAGES_FILE = "/i18n/messages_*.properties";
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^$]*)}");

    private final static Logger LOGGER =
        LoggerFactory.getLogger(TextServiceImpl.class);

    private final Map<String, Map<String, String>> languages;
    private final Environment env;

    TextServiceImpl(Environment env) {
        this.languages = unmodifiableMap(parseLanguageFiles());
        this.env       = requireNonNull(env);
    }

    @Override
    public boolean has(String language) {
        return languages.containsKey(language);
    }

    @Override
    public String get(String key, String language, Map<String, String> args) {
        return getFromDictionary(key, language, args, getDictionary(language));
    }

    private String getFromDictionary(String key,
                                     String language,
                                     Map<String, String> args,
                                     Map<String, String> dictionary) {
        if ("${lang}".equals(key)) {
            return language;
        }

        final String value = dictionary.get(key);
        if (value == null) {
            final String arg = args.get(key);
            if (arg == null) {
                final String prop = env.getProperty(key);
                if (prop == null) {
                    throw new IllegalArgumentException(
                        "Could not resolve parameter ${" + key +
                        "} for language '" + language + "'."
                    );
                }
                return prop;
            } else {
                return formatFromDictionary(arg, language, args, dictionary);
            }
        } else {
            return formatFromDictionary(value, language, args, dictionary);
        }
    }

    @Override
    public String format(String text, String language, Map<String, String> args) {
        return formatFromDictionary(text, language, args, getDictionary(language));
    }

    private String formatFromDictionary(String text,
                                        String language,
                                        Map<String, String> args,
                                        Map<String, String> dictionary) {
        final StringBuilder result = new StringBuilder();
        final Matcher matcher = PATTERN.matcher(text);
        int origin = 0;

        while (matcher.find()) {
            final String key = matcher.group(1);
            final String value = getFromDictionary(key, language, args, dictionary);

            result.append(text.substring(origin, matcher.start()));
            result.append(value);

            origin = matcher.end();
        }

        if (origin < text.length()) {
            result.append(text.substring(origin));
        }

        return result.toString();
    }

    private Map<String, String> getDictionary(String language) {
        final Map<String, String> dictionary = languages.get(language);

        if (dictionary == null) {
            throw new IllegalArgumentException(
                "No language '" + language + "' found."
            );
        }

        return dictionary;
    }

    private Map<String, Map<String, String>> parseLanguageFiles() {
        try {
            return Stream.of(
                new PathMatchingResourcePatternResolver()
                    .getResources(MESSAGES_FILE)
            ).collect(toMap(
                this::parseLanguageName,
                this::parseLanguageFile
            ));
        } catch (final IOException ex) {
            throw new RuntimeException(
                "Error loading resourced by pattern '" +
                MESSAGES_FILE + "'.", ex
            );
        }
    }

    private String parseLanguageName(Resource resource) {
        return substring(
            resource.getFilename(),
            "messages_".length(),
            ".properties".length()
        );
    }

    private Map<String, String> parseLanguageFile(Resource resource) {
        LOGGER.info("Loading i18n file: " + resource.getFilename());

        try {
            final Map<String, String> map = new LinkedHashMap<>();

            Files.lines(Paths.get(resource.getURI()))
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .forEachOrdered(line -> {
                    final int separator = line.indexOf('=');
                    final String key    = line.substring(0, separator).trim();
                    final String value  = line.substring(separator + 1).trim();
                    map.put(key, value);
                });

            return map;
        } catch (final IOException ex) {
            throw new RuntimeException(
                "Could not parse resource '" + resource + "' into an URI.", ex
            );
        }
    }

    private static String substring(String input, int fromIncl, int toExcl) {
        if (toExcl >= 0) {
            return input.substring(fromIncl, toExcl);
        } else {
            final String temp = input.substring(fromIncl);
            return temp.substring(0, temp.length() - toExcl);
        }
    }
}