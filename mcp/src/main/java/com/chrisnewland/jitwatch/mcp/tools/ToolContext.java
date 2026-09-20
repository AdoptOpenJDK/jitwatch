package com.chrisnewland.jitwatch.mcp.tools;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.model.IMetaMember;

import com.chrisnewland.jitwatch.mcp.answer.Budget;
import com.chrisnewland.jitwatch.mcp.query.QueryService;
import com.chrisnewland.jitwatch.mcp.session.Session;
import com.chrisnewland.jitwatch.mcp.session.SessionStore;

/**
 * What every tool needs: the loaded sessions, the answer budget, the readable paths, and
 * the lookups that would otherwise be copied into each tool.
 *
 * The lookups throw {@link IllegalArgumentException} rather than returning null, because
 * a tool that cannot find its session or method has nothing to say and the caller needs
 * to know what to fix.
 */
public final class ToolContext {

	private final SessionStore sessions;

	private final Budget budget;

	private final List<Path> allowedRoots;

	public ToolContext(SessionStore sessions, Budget budget, List<Path> allowedRoots) {
		this.sessions = sessions;
		this.budget = budget;
		this.allowedRoots = allowedRoots;
	}

	public SessionStore sessions() {
		return sessions;
	}

	public Budget budget() {
		return budget;
	}

	public List<Path> allowedRoots() {
		return allowedRoots;
	}

	public File resolve(String path) throws Exception {
		return SessionStore.resolveReadable(path, allowedRoots);
	}

	public List<String> resolveAll(List<String> paths) throws Exception {
		List<String> out = new ArrayList<>();

		if (paths == null) {
			return out;
		}

		for (String path : paths) {
			if (path != null && !path.isBlank()) {
				out.add(resolve(path).getAbsolutePath());
			}
		}

		return out;
	}

	public Session requireSession(String sessionId) throws Exception {
		Session session = sessions.get(sessionId);

		if (session == null) {
			List<String> live = new ArrayList<>();

			for (Session other : sessions.all()) {
				live.add(other.getId());
			}

			throw new IllegalArgumentException("Unknown session: " + sessionId
					+ (live.isEmpty() ? ". Call load_log first." : ". Live sessions: " + String.join(", ", live)));
		}

		return session;
	}

	/**
	 * Offers the nearest few names on a miss, because half-remembered names are the norm.
	 */
	public IMetaMember requireMember(Session session, String signature) throws Exception {
		if (signature == null || signature.isBlank()) {
			throw new IllegalArgumentException("No method given");
		}

		IMetaMember member = session.getIndex().resolve(signature);

		if (member == null) {
			throw new IllegalArgumentException(noMatch(session, signature));
		}

		return member;
	}

	private static String noMatch(Session session, String signature) {
		List<IMetaMember> near = session.getIndex().find(signature, false, 5);

		StringBuilder message = new StringBuilder("No single method matches \"" + signature + "\".");

		if (near.isEmpty()) {
			message.append(" Use find_methods to search.");
		}
		else {
			message.append(" Did you mean: ");

			List<String> names = new ArrayList<>();

			for (IMetaMember candidate : near) {
				names.add(QueryService.signatureOf(candidate));
			}

			message.append(String.join(", ", names));
		}

		return message.toString();
	}

}
