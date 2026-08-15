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
@DebugMetadata(c = "com.example.ui.SchoolViewModel", f = "SchoolViewModel.kt", i = {0, 0}, l = {2059}, m = "sendPasswordResetEmail", n = {"email", "auth"}, s = {"L$0", "L$1"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$sendPasswordResetEmail$1.dex */
public final class SchoolViewModel$sendPasswordResetEmail$1 extends ContinuationImpl {
    Object L$0;
    Object L$1;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$sendPasswordResetEmail$1(SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$sendPasswordResetEmail$1> continuation) {
        super(continuation);
        this.this$0 = schoolViewModel;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        return this.this$0.sendPasswordResetEmail(null, (Continuation) this);
    }
}
