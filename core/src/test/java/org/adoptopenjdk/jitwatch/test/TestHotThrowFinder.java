package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.hotthrow.HotThrowFinder;
import org.adoptopenjdk.jitwatch.hotthrow.HotThrowResult;
import org.adoptopenjdk.jitwatch.model.*;
import org.adoptopenjdk.jitwatch.model.bytecode.ExceptionTable;
import org.adoptopenjdk.jitwatch.model.bytecode.ExceptionTableEntry;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class TestHotThrowFinder {

    private HotThrowFinder hotThrowFinder;
    private IMetaMember mockMetaMember;
    private MemberBytecode mockMemberBytecode;
    private ExceptionTable mockExceptionTable;

    @Before
    public void setUp() {
        IReadOnlyJITDataModel mockModel = Mockito.mock(IReadOnlyJITDataModel.class);
        mockMetaMember = Mockito.mock(IMetaMember.class);
        mockMemberBytecode = Mockito.mock(MemberBytecode.class);
        mockExceptionTable = Mockito.mock(ExceptionTable.class);

        hotThrowFinder = new HotThrowFinder(mockModel);
    }

    @Test
    public void testFindHotThrowsWithNullMember() {
        // Arrange
        IMetaMember member = null;

        // Act
        hotThrowFinder.findHotThrows(null);

        // Assert
        assertTrue(hotThrowFinder.getResult().isEmpty());
    }

    @Test
    public void testFindHotThrowsWithNonNullMemberNoCompilations() {
        // Arrange
        IMetaMember member = mockMetaMember;

        // Mocking behavior
        when(member.getCompilations()).thenReturn(new ArrayList<>());

        // Act
        hotThrowFinder.findHotThrows(member);

        // Assert
        assertTrue(hotThrowFinder.getResult().isEmpty());
    }

    @Test
    public void testFindHotThrowsWithNonNullMemberWithCompilations() {
        // Arrange
        IMetaMember member = mockMetaMember;
        Compilation mockCompilation = Mockito.mock(Compilation.class);

        // Mocking behavior
        when(member.getCompilations()).thenReturn(List.of(mockCompilation));
        when(mockCompilation.getIndex()).thenReturn(1);

        // Act
        hotThrowFinder.findHotThrows(member);

        // Assert
        assertTrue(hotThrowFinder.getResult().isEmpty()); // No mock behavior defined for CompilationUtil
    }

    @Test
    public void testVisitTagWithTagHotThrow() throws LogParseException {
        // Arrange
        Tag mockTag = Mockito.mock(Tag.class);
        IParseDictionary mockParseDictionary = Mockito.mock(IParseDictionary.class);
        Tag mockMethodTag = Mockito.mock(Tag.class);
        Tag mockExceptionTag = Mockito.mock(Tag.class);

        // Mocking behavior
        when(mockTag.getAttributes()).thenReturn(new HashMap<>());
        when(mockTag.getChildren()).thenReturn(List.of(mockMethodTag, mockExceptionTag));
        when(mockMethodTag.getName()).thenReturn("method");
        when(mockMethodTag.getAttributes()).thenReturn(Map.of("name", "testMethod", "holder", "TestClass"));
        when(mockExceptionTag.getName()).thenReturn("hot_throw");
        when(mockExceptionTag.getAttributes()).thenReturn(Map.of("preallocated", "1"));

        // Mock behavior for parseDictionary
        when(mockParseDictionary.getMethod(any())).thenReturn(mockMethodTag);
        when(mockParseDictionary.getKlass(any())).thenReturn(mockTag);

        // Mock behavior for MetaMember and MemberBytecode
        when(mockMetaMember.getMemberBytecode()).thenReturn(mockMemberBytecode);
        when(mockMemberBytecode.getExceptionTable()).thenReturn(mockExceptionTable);
        when(mockExceptionTable.getEntryForBCI(anyInt())).thenReturn(new ExceptionTableEntry(1, 1, 1, ""));

        // Act
        hotThrowFinder.visitTag(mockTag, mockParseDictionary);

        // Assert
        assertFalse(hotThrowFinder.getResult().isEmpty());
        HotThrowResult result = hotThrowFinder.getResult().iterator().next();
        assertEquals("testMethod", result.getMember().getMemberName());
        assertEquals(0, result.getBci());
        assertEquals("ExceptionType", result.getExceptionType()); // Assuming you set this in your code
        assertTrue(result.isPreallocated());
    }
}
