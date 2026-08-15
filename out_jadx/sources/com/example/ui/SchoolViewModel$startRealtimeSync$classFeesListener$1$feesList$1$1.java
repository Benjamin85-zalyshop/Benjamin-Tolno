package com.example.ui;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1.dex */
public final class SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ DocumentSnapshot $doc;
    final /* synthetic */ long $feeAmount;
    final /* synthetic */ String $finalGrade;
    final /* synthetic */ DocumentReference $schoolDocRef;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1(DocumentReference documentReference, String str, long j, DocumentSnapshot documentSnapshot, Continuation<? super SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1> continuation) {
        super(2, continuation);
        this.$schoolDocRef = documentReference;
        this.$finalGrade = str;
        this.$feeAmount = j;
        this.$doc = documentSnapshot;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1(this.$schoolDocRef, this.$finalGrade, this.$feeAmount, this.$doc, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                try {
                    this.$schoolDocRef.collection("classFees").document(this.$finalGrade).set(MapsKt.hashMapOf(new Pair[]{TuplesKt.to("grade", this.$finalGrade), TuplesKt.to("feeAmount", Boxing.boxLong(this.$feeAmount))}));
                    Intrinsics.checkNotNull(this.$doc.getReference().delete());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
