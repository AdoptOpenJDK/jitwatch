package com.chrisnewland.jitwatch.mcp.session;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.bytecode.ClassBC;
import com.chrisnewland.jitwatch.model.bytecode.MemberBytecode;

/**
 * Loads a class's bytecode from the mounted class path, once per class.
 *
 * {@code IMetaMember.getMemberBytecode()} returns whatever is already cached on the
 * MetaClass and null otherwise, because loading needs the class locations, which the
 * model does not hold. The load itself can shell out to javap, so doing it per question
 * would be slow and doing it per answer would be wrong. This does it once and remembers,
 * including remembering failures so a class that cannot be found is not retried on every
 * call.
 */
public final class BytecodeCache {

	private final IReadOnlyJITDataModel model;

	private final List<String> classLocations;

	private final Map<String, Boolean> attempted = new ConcurrentHashMap<>();

	private final Set<String> failed = ConcurrentHashMap.newKeySet();

	public BytecodeCache(IReadOnlyJITDataModel model, List<String> classLocations) {
		this.model = model;
		this.classLocations = classLocations;
	}

	/**
	 * The member's bytecode, loading its class on first use. Null when it cannot be
	 * resolved.
	 */
	public MemberBytecode bytecodeFor(IMetaMember member) {
		if (member == null) {
			return null;
		}

		MetaClass metaClass = member.getMetaClass();

		if (metaClass == null) {
			return null;
		}

		String name = metaClass.getFullyQualifiedName();

		if (failed.contains(name)) {
			return null;
		}

		attempted.computeIfAbsent(name, key -> {
			try {
				ClassBC loaded = metaClass.getClassBytecode(model, classLocations);

				if (loaded == null) {
					failed.add(key);
				}
			}
			catch (Exception e) {
				failed.add(key);
			}

			return Boolean.TRUE;
		});

		try {
			return member.getMemberBytecode();
		}
		catch (Exception e) {
			return null;
		}
	}

	public boolean isMountable() {
		return !classLocations.isEmpty();
	}

}
