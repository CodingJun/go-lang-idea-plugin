/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.util;

import com.goide.sdk.GoSdkService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GoPathResolveScope extends GlobalSearchScope {
  @Nullable private final VirtualFile myReferenceFile;
  @NotNull private final GoPathScopeHelper myScopeHelper;

  public static GlobalSearchScope create(@NotNull Project project, @Nullable Module module, @Nullable PsiElement context) {
    VirtualFile referenceFile = context != null ? context.getContainingFile().getVirtualFile() : null;
    return new GoPathResolveScope(project, referenceFile, GoPathScopeHelper.fromReferenceFile(project, module, referenceFile));
  }

  private GoPathResolveScope(@NotNull Project project,
                             @Nullable VirtualFile referenceFile,
                             @NotNull GoPathScopeHelper scopeHelper) {
    super(project);
    myScopeHelper = scopeHelper;
    myReferenceFile = referenceFile;
  }

  @Override
  public boolean contains(@NotNull VirtualFile declarationFile) {
    VirtualFile declarationDirectory = declarationFile.isDirectory() ? declarationFile : declarationFile.getParent();
    if (declarationDirectory == null) {
      return false;
    }
    if (myReferenceFile != null && declarationDirectory.equals(myReferenceFile.getParent())) {
      return true;
    }
    return myScopeHelper.couldBeReferenced(declarationFile, myReferenceFile);
  }


  @Override
  public int compare(@NotNull VirtualFile file1, @NotNull VirtualFile file2) {
    return 0;
  }

  @Override
  public boolean isSearchInModuleContent(@NotNull Module aModule) {
    return GoSdkService.getInstance(ObjectUtils.assertNotNull(getProject())).isGoModule(aModule);
  }

  @Override
  public boolean isSearchInLibraries() {
    return true;
  }
}