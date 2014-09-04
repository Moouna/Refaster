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
import com.google.common.base.Optional;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.IdentifierTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;

import javax.annotation.Nullable;
import javax.lang.model.element.Name;

/**
 * Identifier corresponding to a template local variable.
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@AutoValue
public abstract class ULocalVarIdent extends UIdent {
  /**
   * A key in a {@code Bindings} associated with a local variable of the specified name.
   */
  public static final class Key extends Bindings.Key<LocalVarBinding> {
    public Key(String name) {
      super(name);
    }
  }
  
  public static ULocalVarIdent create(String identifier) {
    return new AutoValue_ULocalVarIdent(identifier);
  }
  
  abstract String identifier();
  
  Key key() {
    return new Key(identifier());
  }

  @Override
  @Nullable
  public Unifier visitIdentifier(IdentifierTree ident, @Nullable Unifier unifier) {
    LocalVarBinding binding = unifier.getBinding(key());
    return (binding != null && ASTHelpers.getSymbol(ident).equals(binding.getSymbol()))
        ? unifier : null;
  }

  @Override
  public JCIdent inline(Inliner inliner) throws CouldNotResolveImportException {
    Optional<LocalVarBinding> binding = inliner.getOptionalBinding(key());
    return inliner.maker().Ident(binding.isPresent()
        ? binding.get().getName()
        : inliner.asName(identifier()));
  }

  @Override
  public Name getName() {
    return StringName.of(identifier());
  }
}
