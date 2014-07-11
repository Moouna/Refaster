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
import com.google.common.collect.ImmutableList;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;

import java.util.List;

import javax.annotation.Nullable;

/**
 * A representation of a type with optional generic parameters.
 *
 * @author Louis Wasserman
 */
@AutoValue
public abstract class UClassType implements UType {
  public static UClassType create(String fullyQualifiedClass, List<UType> typeArguments) {
    return new AutoValue_UClassType(fullyQualifiedClass, ImmutableList.copyOf(typeArguments));
  }

  public static UClassType create(String fullyQualifiedClass, UType... typeArguments) {
    return create(fullyQualifiedClass, ImmutableList.copyOf(typeArguments));
  }
  
  abstract String fullyQualifiedClass();
  abstract List<UType> typeArguments();

  @Override
  @Nullable
  public Unifier unify(Type target, @Nullable Unifier unifier) {
    if (unifier != null && target instanceof ClassType) {
      ClassType classType = (ClassType) target;
      unifier = classType.tsym.getQualifiedName().contentEquals(fullyQualifiedClass()) 
          ? unifier : null;
      return Unifier.unifyList(unifier, typeArguments(), classType.getTypeArguments());
    }
    return null;
  }
  
  @Override
  public ClassType inline(Inliner inliner) throws CouldNotResolveImportException {
    ClassSymbol classSymbol = inliner.resolveClass(fullyQualifiedClass());
    return new ClassType(
        Type.noType, inliner.<Type, UType>inlineList(typeArguments()), classSymbol);
  }
}
