package org.adoptopenjdk.jitwatch.test;

import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestCompileNode {
    @Test
    public void testAddChild() {
        // Arrange
        CompileNode parent = new CompileNode("parentMethod");
        CompileNode child = new CompileNode("childMethod");

        // Act
        parent.addChild(child);

        // Assert
        assertSame(parent, child.getParent());
        assertEquals(1, parent.getChildren().size());
        assertSame(child, parent.getChildren().get(0));
    }
    @Test
    public void testSetAndGetInlined() {
        // Arrange
        CompileNode node = new CompileNode("testMethod");

        // Act
        node.setInlined(true);

        // Assert
        assertTrue(node.isInlined());
    }
    @Test
    public void testSetAndGetVirtualCall() {
        // Arrange
        CompileNode node = new CompileNode("testMethod");

        // Act
        node.setVirtualCall(true);

        // Assert
        assertTrue(node.isVirtualCall());
    }
    @Test
    public void testSetAndGetTooltipText() {
        // Arrange
        CompileNode node = new CompileNode("testMethod");
        String tooltip = "This is a tooltip";

        // Act
        node.setTooltipText(tooltip);

        // Assert
        assertEquals(tooltip, node.getTooltipText());
    }

    @Test
    public void testGetChildren() {
        // Arrange
        CompileNode parent = new CompileNode("parentMethod");
        CompileNode child1 = new CompileNode("child1Method");
        CompileNode child2 = new CompileNode("child2Method");
        parent.addChild(child1);
        parent.addChild(child2);

        // Act
        List<CompileNode> children = parent.getChildren();

        // Assert
        assertEquals(2, children.size());
        assertSame(child1, children.get(0));
        assertSame(child2, children.get(1));
    }

    @Test
    public void testGetParent() {
        // Arrange
        CompileNode parent = new CompileNode("parentMethod");
        CompileNode child = new CompileNode("childMethod");
        parent.addChild(child);

        // Act
        CompileNode result = child.getParent();

        // Assert
        assertSame(parent, result);
    }
}
