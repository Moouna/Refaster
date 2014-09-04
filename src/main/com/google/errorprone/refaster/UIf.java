/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refaster;

import com.google.auto.value.AutoValue;

import com.sun.source.tree.IfTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.tools.javac.tree.JCTree.JCIf;

import javax.annotation.Nullable;

/**
 * {@link UTree} representation of an {@link IfTree}.
 * 
 * @author lowasser@google.com (Louis Wasserman)
 */
@AutoValue
public abstract class UIf extends UStatement implements IfTree {
  public static UIf create(
      UExpression condition, UStatement thenStatement, UStatement elseStatement) {
    return new AutoValue_UIf(condition, thenStatement, elseStatement);
  }

  @Override
  public abstract UExpression getCondition();

  @Override
  public abstract UStatement getThenStatement();

  @Override
  @Nullable
  public abstract UStatement getElseStatement();

  @Override
  public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
    return visitor.visitIf(this, data);
  }

  @Override
  public Kind getKind() {
    return Kind.IF;
  }

  @Override
  @Nullable
  public Unifier visitIf(IfTree ifTree, @Nullable Unifier unifier) {
    unifier = getCondition().unify(ifTree.getCondition(), unifier);
    unifier = getThenStatement().unify(ifTree.getThenStatement(), unifier);
    return Unifier.unifyNullable(unifier, getElseStatement(), ifTree.getElseStatement());
  }

  @Override
  public JCIf inline(Inliner inliner) throws CouldNotResolveImportException {
    return inliner.maker().If(
        getCondition().inline(inliner),
        getThenStatement().inline(inliner),
        (getElseStatement() == null) ? null : getElseStatement().inline(inliner));
  }
}
