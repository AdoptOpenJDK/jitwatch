/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.bytecode;

import java.util.HashMap;
import java.util.Map;

public enum Opcode implements Comparable<Opcode>
{
	NOP(0, "nop"), ACONST_NULL(1, "aconst_null"), ICONST_M1(2, "iconst_m1"), ICONST_0(3, "iconst_0"), ICONST_1(4,
			"iconst_1"), ICONST_2(5, "iconst_2"), ICONST_3(6, "iconst_3"), ICONST_4(7, "iconst_4"), ICONST_5(8,
					"iconst_5"), LCONST_0(9, "lconst_0"), LCONST_1(10, "lconst_1"), FCONST_0(11, "fconst_0"), FCONST_1(12,
							"fconst_1"), FCONST_2(13, "fconst_2"), DCONST_0(14, "dconst_0"), DCONST_1(15, "dconst_1"), BIPUSH(16,
									"bipush"), SIPUSH(17, "sipush"), LDC(18, "ldc"), LDC_W(19, "ldc_w"), LDC2_W(20,
											"ldc2_w"), ILOAD(21, "iload"), LLOAD(22, "lload"), FLOAD(23, "fload"), DLOAD(24,
													"dload"), ALOAD(25, "aload"), ILOAD_0(26, "iload_0"), ILOAD_1(27,
															"iload_1"), ILOAD_2(28, "iload_2"), ILOAD_3(29, "iload_3"), LLOAD_0(30,
																	"lload_0"), LLOAD_1(31, "lload_1"), LLOAD_2(32,
																			"lload_2"), LLOAD_3(33, "lload_3"), FLOAD_0(34,
																					"fload_0"), FLOAD_1(35, "fload_1"), FLOAD_2(36,
																							"fload_2"), FLOAD_3(37,
																									"fload_3"), DLOAD_0(38,
																											"dload_0"), DLOAD_1(39,
																													"dload_1"), DLOAD_2(
																															40,
																															"dload_2"), DLOAD_3(
																																	41,
																																	"dload_3"), ALOAD_0(
																																			42,
																																			"aload_0"), ALOAD_1(
																																					43,
																																					"aload_1"), ALOAD_2(
																																							44,
																																							"aload_2"), ALOAD_3(
																																									45,
																																									"aload_3"), IALOAD(
																																											46,
																																											"iaload"), LALOAD(
																																													47,
																																													"laload"), FALOAD(
																																															48,
																																															"faload"), DALOAD(
																																																	49,
																																																	"daload"), AALOAD(
																																																			50,
																																																			"aaload"), BALOAD(
																																																					51,
																																																					"baload"), CALOAD(
																																																							52,
																																																							"caload"), SALOAD(
																																																									53,
																																																									"saload"), ISTORE(
																																																											54,
																																																											"istore"), LSTORE(
																																																													55,
																																																													"lstore"), FSTORE(
																																																															56,
																																																															"fstore"), DSTORE(
																																																																	57,
																																																																	"dstore"), ASTORE(
																																																																			58,
																																																																			"astore"), ISTORE_0(
																																																																					59,
																																																																					"istore_0"), ISTORE_1(
																																																																							60,
																																																																							"istore_1"), ISTORE_2(
																																																																									61,
																																																																									"istore_2"), ISTORE_3(
																																																																											62,
																																																																											"istore_3"), LSTORE_0(
																																																																													63,
																																																																													"lstore_0"), LSTORE_1(
																																																																															64,
																																																																															"lstore_1"), LSTORE_2(
																																																																																	65,
																																																																																	"lstore_2"), LSTORE_3(
																																																																																			66,
																																																																																			"lstore_3"), FSTORE_0(
																																																																																					67,
																																																																																					"fstore_0"), FSTORE_1(
																																																																																							68,
																																																																																							"fstore_1"), FSTORE_2(
																																																																																									69,
																																																																																									"fstore_2"), FSTORE_3(
																																																																																											70,
																																																																																											"fstore_3"), DSTORE_0(
																																																																																													71,
																																																																																													"dstore_0"), DSTORE_1(
																																																																																															72,
																																																																																															"dstore_1"), DSTORE_2(
																																																																																																	73,
																																																																																																	"dstore_2"), DSTORE_3(
																																																																																																			74,
																																																																																																			"dstore_3"), ASTORE_0(
																																																																																																					75,
																																																																																																					"astore_0"), ASTORE_1(
																																																																																																							76,
																																																																																																							"astore_1"), ASTORE_2(
																																																																																																									77,
																																																																																																									"astore_2"), ASTORE_3(
																																																																																																											78,
																																																																																																											"astore_3"), IASTORE(
																																																																																																													79,
																																																																																																													"iastore"), LASTORE(
																																																																																																															80,
																																																																																																															"lastore"), FASTORE(
																																																																																																																	81,
																																																																																																																	"fastore"), DASTORE(
																																																																																																																			82,
																																																																																																																			"dastore"), AASTORE(
																																																																																																																					83,
																																																																																																																					"aastore"), BASTORE(
																																																																																																																							84,
																																																																																																																							"bastore"), CASTORE(
																																																																																																																									85,
																																																																																																																									"castore"), SASTORE(
																																																																																																																											86,
																																																																																																																											"sastore"), POP(
																																																																																																																													87,
																																																																																																																													"pop"), POP2(
																																																																																																																															88,
																																																																																																																															"pop2"), DUP(
																																																																																																																																	89,
																																																																																																																																	"dup"), DUP_X1(
																																																																																																																																			90,
																																																																																																																																			"dup_x1"), DUP_X2(
																																																																																																																																					91,
																																																																																																																																					"dup_x2"), DUP2(
																																																																																																																																							92,
																																																																																																																																							"dup2"), DUP2_X1(
																																																																																																																																									93,
																																																																																																																																									"dup2_x1"), DUP2_X2(
																																																																																																																																											94,
																																																																																																																																											"dup2_x2"), SWAP(
																																																																																																																																													95,
																																																																																																																																													"swap"), IADD(
																																																																																																																																															96,
																																																																																																																																															"iadd"), LADD(
																																																																																																																																																	97,
																																																																																																																																																	"ladd"), FADD(
																																																																																																																																																			98,
																																																																																																																																																			"fadd"), DADD(
																																																																																																																																																					99,
																																																																																																																																																					"dadd"), ISUB(
																																																																																																																																																							100,
																																																																																																																																																							"isub"), LSUB(
																																																																																																																																																									101,
																																																																																																																																																									"lsub"), FSUB(
																																																																																																																																																											102,
																																																																																																																																																											"fsub"), DSUB(
																																																																																																																																																													103,
																																																																																																																																																													"dsub"), IMUL(
																																																																																																																																																															104,
																																																																																																																																																															"imul"), LMUL(
																																																																																																																																																																	105,
																																																																																																																																																																	"lmul"), FMUL(
																																																																																																																																																																			106,
																																																																																																																																																																			"fmul"), DMUL(
																																																																																																																																																																					107,
																																																																																																																																																																					"dmul"), IDIV(
																																																																																																																																																																							108,
																																																																																																																																																																							"idiv"), LDIV(
																																																																																																																																																																									109,
																																																																																																																																																																									"ldiv"), FDIV(
																																																																																																																																																																											110,
																																																																																																																																																																											"fdiv"), DDIV(
																																																																																																																																																																													111,
																																																																																																																																																																													"ddiv"), IREM(
																																																																																																																																																																															112,
																																																																																																																																																																															"irem"), LREM(
																																																																																																																																																																																	113,
																																																																																																																																																																																	"lrem"), FREM(
																																																																																																																																																																																			114,
																																																																																																																																																																																			"frem"), DREM(
																																																																																																																																																																																					115,
																																																																																																																																																																																					"drem"), INEG(
																																																																																																																																																																																							116,
																																																																																																																																																																																							"ineg"), LNEG(
																																																																																																																																																																																									117,
																																																																																																																																																																																									"lneg"), FNEG(
																																																																																																																																																																																											118,
																																																																																																																																																																																											"fneg"), DNEG(
																																																																																																																																																																																													119,
																																																																																																																																																																																													"dneg"), ISHL(
																																																																																																																																																																																															120,
																																																																																																																																																																																															"ishl"), LSHL(
																																																																																																																																																																																																	121,
																																																																																																																																																																																																	"lshl"), ISHR(
																																																																																																																																																																																																			122,
																																																																																																																																																																																																			"ishr"), LSHR(
																																																																																																																																																																																																					123,
																																																																																																																																																																																																					"lshr"), IUSHR(
																																																																																																																																																																																																							124,
																																																																																																																																																																																																							"iushr"), LUSHR(
																																																																																																																																																																																																									125,
																																																																																																																																																																																																									"lushr"), IAND(
																																																																																																																																																																																																											126,
																																																																																																																																																																																																											"iand"), LAND(
																																																																																																																																																																																																													127,
																																																																																																																																																																																																													"land"), IOR(
																																																																																																																																																																																																															128,
																																																																																																																																																																																																															"ior"), LOR(
																																																																																																																																																																																																																	129,
																																																																																																																																																																																																																	"lor"), IXOR(
																																																																																																																																																																																																																			130,
																																																																																																																																																																																																																			"ixor"), LXOR(
																																																																																																																																																																																																																					131,
																																																																																																																																																																																																																					"lxor"), IINC(
																																																																																																																																																																																																																							132,
																																																																																																																																																																																																																							"iinc"), I2L(
																																																																																																																																																																																																																									133,
																																																																																																																																																																																																																									"i2l"), I2F(
																																																																																																																																																																																																																											134,
																																																																																																																																																																																																																											"i2f"), I2D(
																																																																																																																																																																																																																													135,
																																																																																																																																																																																																																													"i2d"), L2I(
																																																																																																																																																																																																																															136,
																																																																																																																																																																																																																															"l2i"), L2F(
																																																																																																																																																																																																																																	137,
																																																																																																																																																																																																																																	"l2f"), L2D(
																																																																																																																																																																																																																																			138,
																																																																																																																																																																																																																																			"l2d"), F2I(
																																																																																																																																																																																																																																					139,
																																																																																																																																																																																																																																					"f2i"), F2L(
																																																																																																																																																																																																																																							140,
																																																																																																																																																																																																																																							"f2l"), F2D(
																																																																																																																																																																																																																																									141,
																																																																																																																																																																																																																																									"f2d"), D2I(
																																																																																																																																																																																																																																											142,
																																																																																																																																																																																																																																											"d2i"), D2L(
																																																																																																																																																																																																																																													143,
																																																																																																																																																																																																																																													"d2l"), D2F(
																																																																																																																																																																																																																																															144,
																																																																																																																																																																																																																																															"d2f"), I2B(
																																																																																																																																																																																																																																																	145,
																																																																																																																																																																																																																																																	"i2b"), I2C(
																																																																																																																																																																																																																																																			146,
																																																																																																																																																																																																																																																			"i2c"), I2S(
																																																																																																																																																																																																																																																					147,
																																																																																																																																																																																																																																																					"i2s"), LCMP(
																																																																																																																																																																																																																																																							148,
																																																																																																																																																																																																																																																							"lcmp"), FCMPL(
																																																																																																																																																																																																																																																									149,
																																																																																																																																																																																																																																																									"fcmpl"), FCMPG(
																																																																																																																																																																																																																																																											150,
																																																																																																																																																																																																																																																											"fcmpg"), DCMPL(
																																																																																																																																																																																																																																																													151,
																																																																																																																																																																																																																																																													"dcmpl"), DCMPG(
																																																																																																																																																																																																																																																															152,
																																																																																																																																																																																																																																																															"dcmpg"), IFEQ(
																																																																																																																																																																																																																																																																	153,
																																																																																																																																																																																																																																																																	"ifeq"), IFNE(
																																																																																																																																																																																																																																																																			154,
																																																																																																																																																																																																																																																																			"ifne"), IFLT(
																																																																																																																																																																																																																																																																					155,
																																																																																																																																																																																																																																																																					"iflt"), IFGE(
																																																																																																																																																																																																																																																																							156,
																																																																																																																																																																																																																																																																							"ifge"), IFGT(
																																																																																																																																																																																																																																																																									157,
																																																																																																																																																																																																																																																																									"ifgt"), IFLE(
																																																																																																																																																																																																																																																																											158,
																																																																																																																																																																																																																																																																											"ifle"), IF_ICMPEQ(
																																																																																																																																																																																																																																																																													159,
																																																																																																																																																																																																																																																																													"if_icmpeq"), IF_ICMPNE(
																																																																																																																																																																																																																																																																															160,
																																																																																																																																																																																																																																																																															"if_icmpne"), IF_ICMPLT(
																																																																																																																																																																																																																																																																																	161,
																																																																																																																																																																																																																																																																																	"if_icmplt"), IF_ICMPGE(
																																																																																																																																																																																																																																																																																			162,
																																																																																																																																																																																																																																																																																			"if_icmpge"), IF_ICMPGT(
																																																																																																																																																																																																																																																																																					163,
																																																																																																																																																																																																																																																																																					"if_icmpgt"), IF_ICMPLE(
																																																																																																																																																																																																																																																																																							164,
																																																																																																																																																																																																																																																																																							"if_icmple"), IF_ACMPEQ(
																																																																																																																																																																																																																																																																																									165,
																																																																																																																																																																																																																																																																																									"if_acmpeq"), IF_ACMPNE(
																																																																																																																																																																																																																																																																																											166,
																																																																																																																																																																																																																																																																																											"if_acmpne"), GOTO(
																																																																																																																																																																																																																																																																																													167,
																																																																																																																																																																																																																																																																																													"goto"), JSR(
																																																																																																																																																																																																																																																																																															168,
																																																																																																																																																																																																																																																																																															"jsr"), RET(
																																																																																																																																																																																																																																																																																																	169,
																																																																																																																																																																																																																																																																																																	"ret"), TABLESWITCH(
																																																																																																																																																																																																																																																																																																			170,
																																																																																																																																																																																																																																																																																																			"tableswitch"), LOOKUPSWITCH(
																																																																																																																																																																																																																																																																																																					171,
																																																																																																																																																																																																																																																																																																					"lookupswitch"), IRETURN(
																																																																																																																																																																																																																																																																																																							172,
																																																																																																																																																																																																																																																																																																							"ireturn"), LRETURN(
																																																																																																																																																																																																																																																																																																									173,
																																																																																																																																																																																																																																																																																																									"lreturn"), FRETURN(
																																																																																																																																																																																																																																																																																																											174,
																																																																																																																																																																																																																																																																																																											"freturn"), DRETURN(
																																																																																																																																																																																																																																																																																																													175,
																																																																																																																																																																																																																																																																																																													"dreturn"), ARETURN(
																																																																																																																																																																																																																																																																																																															176,
																																																																																																																																																																																																																																																																																																															"areturn"), RETURN(
																																																																																																																																																																																																																																																																																																																	177,
																																																																																																																																																																																																																																																																																																																	"return"), GETSTATIC(
																																																																																																																																																																																																																																																																																																																			178,
																																																																																																																																																																																																																																																																																																																			"getstatic"), PUTSTATIC(
																																																																																																																																																																																																																																																																																																																					179,
																																																																																																																																																																																																																																																																																																																					"putstatic"), GETFIELD(
																																																																																																																																																																																																																																																																																																																							180,
																																																																																																																																																																																																																																																																																																																							"getfield"), PUTFIELD(
																																																																																																																																																																																																																																																																																																																									181,
																																																																																																																																																																																																																																																																																																																									"putfield"), INVOKEVIRTUAL(
																																																																																																																																																																																																																																																																																																																											182,
																																																																																																																																																																																																																																																																																																																											"invokevirtual"), INVOKESPECIAL(
																																																																																																																																																																																																																																																																																																																													183,
																																																																																																																																																																																																																																																																																																																													"invokespecial"), INVOKESTATIC(
																																																																																																																																																																																																																																																																																																																															184,
																																																																																																																																																																																																																																																																																																																															"invokestatic"), INVOKEINTERFACE(
																																																																																																																																																																																																																																																																																																																																	185,
																																																																																																																																																																																																																																																																																																																																	"invokeinterface"), INVOKEDYNAMIC(
																																																																																																																																																																																																																																																																																																																																			186,
																																																																																																																																																																																																																																																																																																																																			"invokedynamic"), NEW(
																																																																																																																																																																																																																																																																																																																																					187,
																																																																																																																																																																																																																																																																																																																																					"new"), NEWARRAY(
																																																																																																																																																																																																																																																																																																																																							188,
																																																																																																																																																																																																																																																																																																																																							"newarray"), ANEWARRAY(
																																																																																																																																																																																																																																																																																																																																									189,
																																																																																																																																																																																																																																																																																																																																									"anewarray"), ARRAYLENGTH(
																																																																																																																																																																																																																																																																																																																																											190,
																																																																																																																																																																																																																																																																																																																																											"arraylength"), ATHROW(
																																																																																																																																																																																																																																																																																																																																													191,
																																																																																																																																																																																																																																																																																																																																													"athrow"), CHECKCAST(
																																																																																																																																																																																																																																																																																																																																															192,
																																																																																																																																																																																																																																																																																																																																															"checkcast"), INSTANCEOF(
																																																																																																																																																																																																																																																																																																																																																	193,
																																																																																																																																																																																																																																																																																																																																																	"instanceof"), MONITORENTER(
																																																																																																																																																																																																																																																																																																																																																			194,
																																																																																																																																																																																																																																																																																																																																																			"monitorenter"), MONITOREXIT(
																																																																																																																																																																																																																																																																																																																																																					195,
																																																																																																																																																																																																																																																																																																																																																					"monitorexit"), WIDE(
																																																																																																																																																																																																																																																																																																																																																							196,
																																																																																																																																																																																																																																																																																																																																																							"wide"), MULTIANEWARRAY(
																																																																																																																																																																																																																																																																																																																																																									197,
																																																																																																																																																																																																																																																																																																																																																									"multianewarray"), IFNULL(
																																																																																																																																																																																																																																																																																																																																																											198,
																																																																																																																																																																																																																																																																																																																																																											"ifnull"), IFNONNULL(
																																																																																																																																																																																																																																																																																																																																																													199,
																																																																																																																																																																																																																																																																																																																																																													"ifnonnull"), GOTO_W(
																																																																																																																																																																																																																																																																																																																																																															200,
																																																																																																																																																																																																																																																																																																																																																															"goto_w"), JSR_W(
																																																																																																																																																																																																																																																																																																																																																																	201,
																																																																																																																																																																																																																																																																																																																																																																	"jsr_w"), BREAKPOINT(
																																																																																																																																																																																																																																																																																																																																																																			202,
																																																																																																																																																																																																																																																																																																																																																																			"breakpoint"), IMPDEP1(
																																																																																																																																																																																																																																																																																																																																																																					254,
																																																																																																																																																																																																																																																																																																																																																																					"impdep1"), IMPDEP2(
																																																																																																																																																																																																																																																																																																																																																																							255,
																																																																																																																																																																																																																																																																																																																																																																							"impdep2");

	Opcode(int code, String text)
	{
		this.code = code;
		this.mnemonic = text;
	}

	private final int code;
	private final String mnemonic;

	public int getCode()
	{
		return code;
	}

	public String getMnemonic()
	{
		return mnemonic;
	}

	static final Map<String, Opcode> mnemonicMap = new HashMap<>();
	static final Map<Integer, Opcode> opcodeMap = new HashMap<>();

	static
	{
		for (Opcode oc : Opcode.values())
		{
			mnemonicMap.put(oc.getMnemonic(), oc);
			opcodeMap.put(oc.getCode(), oc);

		}
	}

	public boolean isInvoke()
	{
		return this == Opcode.INVOKEVIRTUAL || this == Opcode.INVOKESPECIAL || this == Opcode.INVOKESTATIC
				|| this == Opcode.INVOKEINTERFACE || this == Opcode.INVOKEDYNAMIC;
	}

	public boolean isLock()
	{
		return this == Opcode.MONITORENTER;
	}

	public boolean isSwitch()
	{
		return this == Opcode.TABLESWITCH || this == Opcode.LOOKUPSWITCH;
	}

	public boolean isAllocation()
	{
		return this == NEW || this == Opcode.NEWARRAY || this == ANEWARRAY || this == MULTIANEWARRAY;
	}

	public static Opcode getByMnemonic(String mnemonic)
	{
		return mnemonicMap.get(mnemonic);
	}
	
	public static Opcode getByCode(int code)
	{
		return opcodeMap.get(code);
	}

	public boolean equals(String mnemonic)
	{
		return this.mnemonic.equals(mnemonic);
	}
}