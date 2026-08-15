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
@DebugMetadata(c = "com.example.ui.SchoolViewModel", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5}, l = {1437, 1444, 1447, 1464, 1466, 1467}, m = "registerSchool", n = {"email", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "cleanEmail", "auth", "email", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "cleanEmail", "auth", "result", "financierEmail", "email", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "cleanEmail", "auth", "result", "financierEmail", "email", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "cleanEmail", "auth", "result", "db", "schoolData", "email", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "cleanEmail", "auth", "result", "db", "schoolData", "email", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "cleanEmail", "auth", "result", "db", "schoolData"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$registerSchool$1.dex */
public final class SchoolViewModel$registerSchool$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$registerSchool$1(SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$registerSchool$1> continuation) {
        super(continuation);
        this.this$0 = schoolViewModel;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.registerSchool(null, null, null, null, null, null, (Continuation) this);
    }
}
