package com.example.ui;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function3;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00012\b\u0010\u0003\u001a\u0004\u0018\u00010\u0001H\n"}, d2 = {"<anonymous>", "", "collected", "expenses"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$balance$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$balance$1.dex */
public final class SchoolViewModel$balance$1 extends SuspendLambda implements Function3<Long, Long, Continuation<? super Long>, Object> {
    /* synthetic */ Object L$0;
    /* synthetic */ Object L$1;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SchoolViewModel$balance$1(Continuation<? super SchoolViewModel$balance$1> continuation) {
        super(3, continuation);
    }

    public final Object invoke(Long l, Long l2, Continuation<? super Long> continuation) {
        SchoolViewModel$balance$1 schoolViewModel$balance$1 = new SchoolViewModel$balance$1(continuation);
        schoolViewModel$balance$1.L$0 = l;
        schoolViewModel$balance$1.L$1 = l2;
        return schoolViewModel$balance$1.invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        Long collected = (Long) this.L$0;
        Long expenses = (Long) this.L$1;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                return Boxing.boxLong((collected != null ? collected.longValue() : 0L) - (expenses != null ? expenses.longValue() : 0L));
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
