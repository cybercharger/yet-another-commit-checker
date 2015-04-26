package com.isroot.stash.plugin;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.setting.Settings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

/**
 * @author Sean Ford
 * @since 2013-05-11
 */
public final class YaccHook implements PreReceiveRepositoryHook {
    private static final Logger log = LoggerFactory.getLogger(YaccHook.class);

    public static final String ERROR_BEARS = "\n" +
            "  (c).-.(c)    (c).-.(c)    (c).-.(c)    (c).-.(c)    (c).-.(c) \n" +
            "   / ._. \\      / ._. \\      / ._. \\      / ._. \\      / ._. \\ \n" +
            " __\\( Y )/__  __\\( Y )/__  __\\( Y )/__  __\\( Y )/__  __\\( Y )/__\n" +
            "(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)\n" +
            "   || E ||      || R ||      || R ||      || O ||      || R ||\n" +
            " _.' `-' '._  _.' `-' '._  _.' `-' '._  _.' `-' '._  _.' `-' '.\n" +
            "(.-./`-'\\.-.)(.-./`-`\\.-.)(.-./`-`\\.-.)(.-./`-'\\.-.)(.-./`-`\\.-.)\n" +
            " `-'     `-'  `-'     `-'  `-'     `-'  `-'     `-'  `-'     `-' \n" +
            "\n" +
            "\n" +
            "Push rejected.\n";

    private final YaccService yaccService;

    public YaccHook(YaccService yaccService) {
        this.yaccService = yaccService;
    }

    @Override
    public boolean onReceive(@Nonnull RepositoryHookContext repositoryHookContext,
                             @Nonnull Collection<RefChange> refChanges, @Nonnull HookResponse hookResponse) {
        List<String> errors = Lists.newArrayList();
        Settings settings = repositoryHookContext.getSettings();

        for (RefChange rf : refChanges) {
            if (rf.getType() == RefChangeType.DELETE) {
                continue;
            }

            errors.addAll(yaccService.checkRefChange(repositoryHookContext.getRepository(),
                    settings, rf));
        }

        if (errors.isEmpty()) {
            log.debug("push allowed");

            return true;
        } else {
            printHeader(settings, hookResponse.err());

            for (String error : errors) {
                log.debug("error: {}", error);

                hookResponse.err().println(error);
            }

            hookResponse.err().println();

            printFooter(settings, hookResponse.err());

            log.debug("push rejected");

            return false;
        }
    }

    private void printHeader(Settings settings, PrintWriter writer) {
        String header = settings.getString("errorMessageHeader");

        if(header == null || header.isEmpty()) {
            // sford: long live the error bears
            header = ERROR_BEARS;
        }

        writer.println(header);
        writer.println();
    }

    private void printFooter(Settings settings, PrintWriter writer) {
        String footer = settings.getString("errorMessageFooter");
        if(footer != null && !footer.isEmpty()) {
            writer.println(footer);
            writer.println();
        }
    }
}
