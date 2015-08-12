package org.adoptopenjdk.jitwatch.model.bytecode;

public enum BCAnnotationType
{
	BRANCH, INLINE_SUCCESS, INLINE_FAIL, ELIMINATED_ALLOCATION, LOCK_ELISION, LOCK_COARSEN, INTRINSIC_USED, UNCOMMON_TRAP;
}