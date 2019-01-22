/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

/** Logger. */
public class Logger implements Closeable, Flushable
{
    /** The Maven log to delegate to. */
    private final Log mavenLog;

    /** The print writers to copy logs to. */
    private final List<PrintWriter> printWriters = new ArrayList<>(4);

    /**
     * Constructor.
     *
     * @param mavenLog the Maven log.
     * @param logFile the file to copy logs to.
     */
    public Logger(final Log mavenLog, final File logFile)
    {
        this.mavenLog = mavenLog;
        addOutputFile(logFile);
    }

    /**
     * Adds the given file for log output.
     *
     * @param file the file to output logs to.
     * @return the print writer.
     */
    public PrintWriter addOutputFile(final File file)
    {
        try
        {
            FileUtils.forceMkdir(file.getParentFile());
            if (!file.exists() && !file.createNewFile())
            {
                throw new RuntimeException("Could not create log output file " + file.getPath() + ".");
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            final PrintWriter printWriter = new PrintWriter(outputStreamWriter);
            printWriters.add(printWriter);
            return printWriter;
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close()
    {
        printWriters.forEach(PrintWriter::close);
    }

    /**
     * Logs the given message at debug level.
     *
     * @param content the message to log.
     */
    public void debug(final CharSequence content)
    {
        mavenLog.debug(content);
    }

    /**
     * Logs the given message and accompanying exception at debug level.
     *
     * @param content the message to log.
     * @param error the associated exception.
     */
    public void debug(final CharSequence content, final Throwable error)
    {
        mavenLog.debug(content, error);
    }

    /**
     * Logs the given exception at debug level.
     *
     * @param error the exception to log.
     */
    public void debug(final Throwable error)
    {
        mavenLog.debug(error);
    }

    /**
     * Logs the given message at error level.
     *
     * @param content the message to log.
     */
    public void error(final CharSequence content)
    {
        mavenLog.error(content);
        printWriters.forEach(w -> w.println("[ERROR] " + content));
    }

    /**
     * Logs the given message and accompanying exception at error level.
     *
     * @param content the message to log.
     * @param error the associated exception.
     */
    public void error(final CharSequence content, final Throwable error)
    {
        mavenLog.error(content, error);
    }

    /**
     * Logs the given exception at error level.
     *
     * @param error the exception to log.
     */
    public void error(final Throwable error)
    {
        mavenLog.error(error);
    }

    @Override
    public void flush()
    {
        printWriters.forEach(PrintWriter::flush);
    }

    /**
     * Logs the given message at info level.
     *
     * @param content the exception to log.
     */
    public void info(final CharSequence content)
    {
        mavenLog.info(content);
        printWriters.forEach(w -> w.println(content));
    }

    /**
     * Logs the given message and accompanying exception at info level.
     *
     * @param content the message to log.
     * @param error the associated exception.
     */
    public void info(final CharSequence content, final Throwable error)
    {
        mavenLog.info(content, error);
    }

    /**
     * Logs the given exception at info level.
     *
     * @param error the exception to log.
     */
    public void info(final Throwable error)
    {
        mavenLog.info(error);
    }

    /**
     * Returns whether debug logging is enabled.
     *
     * @return whether debug logging is enabled.
     */
    public boolean isDebugEnabled()
    {
        return mavenLog.isDebugEnabled();
    }

    /**
     * Returns whether error logging is enabled.
     *
     * @return whether error level logging is enabled.
     */
    public boolean isErrorEnabled()
    {
        return mavenLog.isErrorEnabled();
    }

    /**
     * Returns whether info level logging is enabled.
     *
     * @return whether info level logging is enabled.
     */
    public boolean isInfoEnabled()
    {
        return mavenLog.isInfoEnabled();
    }

    /**
     * Returns whether warn level logging is enabled.
     *
     * @return whether warn level logging is enabled.
     */
    public boolean isWarnEnabled()
    {
        return mavenLog.isWarnEnabled();
    }

    /**
     * Logs the given message at warn level.
     *
     * @param content the message to log.
     */
    public void warn(final CharSequence content)
    {
        mavenLog.warn(content);
        printWriters.forEach(w -> w.println("[WARNING] " + content));
    }

    /**
     * Logs the given message and accompanying exception at warn level.
     *
     * @param content the message to log.
     * @param error the associated exception.
     */
    public void warn(final CharSequence content, final Throwable error)
    {
        mavenLog.warn(content, error);
    }

    /**
     * Logs the given exception at warn level.
     *
     * @param error the exception to log.
     */
    public void warn(final Throwable error)
    {
        mavenLog.warn(error);
    }
}