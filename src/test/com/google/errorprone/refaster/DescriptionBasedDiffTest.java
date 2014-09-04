/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.errorprone.refaster;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.apply.DescriptionBasedDiff;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Tests for {@code DescriptionBasedDiff}.
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@RunWith(JUnit4.class)
public class DescriptionBasedDiffTest extends CompilerBasedTest {
  private JCCompilationUnit compilationUnit;

  private static final String[] lines = {
    "package foo.bar;",
    "class Foo {",
    "  public static void main(String[] args) {",
    "    System.out.println(\"foo\");",
    "  }", 
    "}"};

  @Before
  public void setUp() {
    compile(lines);
    compilationUnit = Iterables.getOnlyElement(compilationUnits);
  }

  @Test
  public void noDiffs() {
    DescriptionBasedDiff diff = DescriptionBasedDiff.create(compilationUnit);
    diff.applyDifferences(sourceFile);
    assertThat(sourceFile.getLines()).iteratesAs(Arrays.asList(lines));
  }

  @Test
  public void oneDiff() {
    DescriptionBasedDiff diff = DescriptionBasedDiff.create(compilationUnit);
    diff.onDescribed(new Description(null, "message", SuggestedFix.replace(96, 99, "bar"),
        SeverityLevel.NOT_A_PROBLEM));
    diff.applyDifferences(sourceFile);
    assertThat(sourceFile.getLines()).iteratesAs(
        "package foo.bar;",
        "class Foo {",
        "  public static void main(String[] args) {",
        "    System.out.println(\"bar\");",
        "  }", 
        "}");
  }

  @Test
  public void twoDiffs() {
    DescriptionBasedDiff diff = DescriptionBasedDiff.create(compilationUnit);
    diff.onDescribed(new Description(null, "message", 
        SuggestedFix.builder()
            .replace(83, 86, "longer")
            .replace(96, 99, "bar")
            .build(),
        SeverityLevel.NOT_A_PROBLEM));
    diff.applyDifferences(sourceFile);
    assertThat(sourceFile.getLines()).iteratesAs(
        "package foo.bar;",
        "class Foo {",
        "  public static void main(String[] args) {",
        "    System.longer.println(\"bar\");",
        "  }", 
        "}");
  }

  @Test
  public void addImport() {
    DescriptionBasedDiff diff = DescriptionBasedDiff.create(compilationUnit);
    diff.onDescribed(new Description(null, "message", 
        SuggestedFix.builder().addImport("com.google.foo.Bar").build(),
        SeverityLevel.NOT_A_PROBLEM));
    diff.applyDifferences(sourceFile);
    assertThat(sourceFile.getLines()).iteratesAs(
        "package foo.bar;",
        "",
        "import com.google.foo.Bar;",
        "class Foo {",
        "  public static void main(String[] args) {",
        "    System.out.println(\"foo\");",
        "  }", 
        "}");
  }

  @Test
  public void twoDiffsWithImport() {
    DescriptionBasedDiff diff = DescriptionBasedDiff.create(compilationUnit);
    diff.onDescribed(new Description(null, "message", 
        SuggestedFix.builder()
            .replace(83, 86, "longer")
            .replace(96, 99, "bar")
            .addImport("com.google.foo.Bar")
            .build(),
        SeverityLevel.NOT_A_PROBLEM));
    diff.applyDifferences(sourceFile);
    assertThat(sourceFile.getLines()).iteratesAs(
        "package foo.bar;",
        "",
        "import com.google.foo.Bar;",
        "class Foo {",
        "  public static void main(String[] args) {",
        "    System.longer.println(\"bar\");",
        "  }", 
        "}");
  }
}
