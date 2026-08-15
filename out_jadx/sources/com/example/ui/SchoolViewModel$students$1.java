package com.example.ui;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Triple;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function4;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0010\u000e\n\u0002\b\u0003\u0010\u0000\u001a\u0016\u0012\u0006\u0012\u0004\u0018\u00010\u0002\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u00012\b\u0010\u0004\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0003H\n"}, d2 = {"<anonymous>", "Lkotlin/Triple;", "", "", "id", "section", "year"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$students$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$students$1.dex */
public final class SchoolViewModel$students$1 extends SuspendLambda implements Function4<Integer, String, String, Continuation<? super Triple<? extends Integer, ? extends String, ? extends String>>, Object> {
    /* synthetic */ Object L$0;
    /* synthetic */ Object L$1;
    /* synthetic */ Object L$2;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SchoolViewModel$students$1(Continuation<? super SchoolViewModel$students$1> continuation) {
        super(4, continuation);
    }

    public final Object invoke(Integer num, String str, String str2, Continuation<? super Triple<Integer, String, String>> continuation) {
        SchoolViewModel$students$1 schoolViewModel$students$1 = new SchoolViewModel$students$1(continuation);
        schoolViewModel$students$1.L$0 = num;
        schoolViewModel$students$1.L$1 = str;
        schoolViewModel$students$1.L$2 = str2;
        return schoolViewModel$students$1.invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        Integer id = (Integer) this.L$0;
        String section = (String) this.L$1;
        String year = (String) this.L$2;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                return new Triple(id, section, year);
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
