package hudson.plugins.im.build_notify;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.im.IMPublisher;
import hudson.plugins.im.tools.BuildHelper;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.io.IOException;

/**
 * Custom group message {@link BuildToChatNotifier}. Used in case the project defines custom group chat messages.
 * When no custom message is defined, calls the {}@link #selectedNotifier} to create the message.
 *
 *
 * @author reynald
 */
public class CustomGroupMessageNotifier extends BuildToChatNotifier {

	private final BuildToChatNotifier selectedNotifier;

	public CustomGroupMessageNotifier(final BuildToChatNotifier selectedNotifier) {
		this.selectedNotifier = selectedNotifier;
	}

	@Override
	public String buildStartMessage(final IMPublisher publisher, final AbstractBuild<?, ?> build, final BuildListener listener) throws IOException, InterruptedException {
		String message = publisher.getCustomStartMessage();
		if (StringUtils.isEmpty(message)) {
			message = selectedNotifier.buildStartMessage(publisher, build, listener);
		}

		return replaceTokensInMessage(message, build, listener);
	}

	@Override
	public String buildCompletionMessage(final IMPublisher publisher, final AbstractBuild<?, ?> build, final BuildListener listener) throws IOException, InterruptedException {
		String message;
		if (BuildHelper.isFix(build)) {
			message = publisher.getCustomFixedMessage();
		} else if (build.getResult() == Result.SUCCESS) {
			message = publisher.getCustomSuccessMessage();
		} else if (build.getResult() == Result.UNSTABLE) {
			message = publisher.getCustomUnstableMessage();
		} else if (build.getResult() == Result.FAILURE) {
			message = publisher.getCustomFailedMessage();
		} else {
			message = null;
		}

		if (StringUtils.isEmpty(message)) {
			message = selectedNotifier.buildCompletionMessage(publisher, build, listener);
		}

		return replaceTokensInMessage(message, build, listener);
	}

	private String replaceTokensInMessage(final String message, final AbstractBuild<?, ?> build,
										  final BuildListener listener) {

		listener.getLogger().println("Replacing tokens in message:'" + message + "'");

		try {
			return TokenMacro.expand(build, listener, message);
		} catch (MacroEvaluationException mee) {
			throw new RuntimeException(mee);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}
}
