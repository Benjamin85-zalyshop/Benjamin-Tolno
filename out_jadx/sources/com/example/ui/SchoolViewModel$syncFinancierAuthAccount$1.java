package com.example.ui;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4}, l = {1496, 1498, 1502, 1511, 1519}, m = "syncFinancierAuthAccount", n = {"schoolEmail", "founderPassword", "financierPassword", "auth", "currentAuthUser", "currentEmail", "cleanEmail", "financierEmail", "schoolEmail", "founderPassword", "financierPassword", "auth", "currentAuthUser", "currentEmail", "cleanEmail", "financierEmail", "schoolEmail", "founderPassword", "financierPassword", "auth", "currentAuthUser", "currentEmail", "cleanEmail", "financierEmail", "signInEx", "schoolEmail", "founderPassword", "financierPassword", "auth", "currentAuthUser", "currentEmail", "cleanEmail", "financierEmail", "schoolEmail", "founderPassword", "financierPassword", "auth", "currentAuthUser", "currentEmail", "cleanEmail", "financierEmail", "e"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$syncFinancierAuthAccount$1.dex */
public final class SchoolViewModel$syncFinancierAuthAccount$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$syncFinancierAuthAccount$1(SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$syncFinancierAuthAccount$1> continuation) {
        super(continuation);
        this.this$0 = schoolViewModel;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object syncFinancierAuthAccount;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        syncFinancierAuthAccount = this.this$0.syncFinancierAuthAccount(null, null, null, (Continuation) this);
        return syncFinancierAuthAccount;
    }
}
