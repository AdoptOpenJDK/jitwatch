package com.chrisnewland.jitwatch.mcp.session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;

/**
 * Free-text lookup from a partial name to the members in a log.
 *
 * An agent asks about "chainA4" or "MakeHotSpotLog.bigMethod", never about
 * {@code com.chrisnewland.jitwatch.demo.MakeHotSpotLog bigMethod (JI)J}. This walks the
 * parsed model once and answers those questions without the caller needing an exact
 * signature.
 */
public final class SignatureIndex {

	private record Entry(IMetaMember member, String fqMember, String simpleClass, String memberName, String haystack) {
	}

	private final List<Entry> entries = new ArrayList<>();

	public SignatureIndex(IReadOnlyJITDataModel model) {
		for (MetaPackage root : model.getPackageManager().getRootPackages()) {
			walk(root);
		}
	}

	private void walk(MetaPackage metaPackage) {
		for (MetaClass metaClass : metaPackage.getPackageClasses()) {
			for (IMetaMember member : metaClass.getMetaMembers()) {
				String fqClass = metaClass.getFullyQualifiedName();
				String simpleClass = metaClass.getName();
				String memberName = member.getMemberName();
				String fqMember = fqClass + "." + memberName;

				String haystack = (fqMember + " " + member.toStringUnqualifiedMethodName(false, false))
					.toLowerCase(Locale.ROOT);

				entries.add(new Entry(member, fqMember, simpleClass, memberName, haystack));
			}
		}

		for (MetaPackage child : metaPackage.getChildPackages()) {
			walk(child);
		}
	}

	public int size() {
		return entries.size();
	}

	/**
	 * Members matching a free-text query, best match first.
	 *
	 * Ranking, in order: exact member name, exact Class.member, the query appearing at a
	 * word boundary, then anywhere. Compiled members outrank uncompiled ones at equal
	 * relevance, because a member with no compilations has nothing to say about the JIT.
	 */
	public List<IMetaMember> find(String query, boolean compiledOnly, int limit) {
		String needle = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();

		List<Entry> matches = new ArrayList<>();

		for (Entry entry : entries) {
			if (compiledOnly && !entry.member().isCompiled()) {
				continue;
			}

			if (needle.isEmpty() || entry.haystack().contains(needle)) {
				matches.add(entry);
			}
		}

		matches.sort(Comparator.comparingInt((Entry e) -> -score(e, needle))
			.thenComparing(e -> e.member().isCompiled() ? 0 : 1)
			.thenComparing(Entry::fqMember));

		List<IMetaMember> out = new ArrayList<>();

		for (Entry entry : matches) {
			if (out.size() >= limit) {
				break;
			}

			out.add(entry.member());
		}

		return out;
	}

	private static int score(Entry entry, String needle) {
		if (needle.isEmpty()) {
			return 0;
		}

		String memberName = entry.memberName().toLowerCase(Locale.ROOT);
		String classDotMember = (entry.simpleClass() + "." + entry.memberName()).toLowerCase(Locale.ROOT);

		if (memberName.equals(needle)) {
			return 100;
		}

		if (classDotMember.equals(needle) || entry.fqMember().toLowerCase(Locale.ROOT).equals(needle)) {
			return 95;
		}

		if (memberName.startsWith(needle)) {
			return 70;
		}

		if (classDotMember.contains(needle)) {
			return 50;
		}

		return 10;
	}

	/**
	 * The single member a caller meant, or null.
	 *
	 * Accepts a fully qualified signature, {@code Class.member}, or a bare member name as
	 * long as it is unambiguous among compiled members.
	 */
	public IMetaMember resolve(String query) {
		List<IMetaMember> compiled = find(query, true, 2);

		if (compiled.size() == 1) {
			return compiled.get(0);
		}

		if (compiled.size() > 1) {
			// Ambiguous among compiled members: take an exact hit if there is exactly one
			return exactOrNull(query, true);
		}

		List<IMetaMember> any = find(query, false, 2);

		if (any.size() == 1) {
			return any.get(0);
		}

		return any.isEmpty() ? null : exactOrNull(query, false);
	}

	private IMetaMember exactOrNull(String query, boolean compiledOnly) {
		String needle = query.toLowerCase(Locale.ROOT).trim();

		IMetaMember hit = null;

		for (Entry entry : entries) {
			if (compiledOnly && !entry.member().isCompiled()) {
				continue;
			}

			if (score(entry, needle) >= 95) {
				if (hit != null) {
					return null;
				}

				hit = entry.member();
			}
		}

		return hit;
	}

}
