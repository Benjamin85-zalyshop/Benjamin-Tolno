package com.example.ui;

import com.example.data.repository.SchoolRepository;
import com.google.firebase.firestore.FirebaseFirestore;
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
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$submitSubscriptionRequest$1", f = "SchoolViewModel.kt", i = {0}, l = {2052}, m = "invokeSuspend", n = {"db"}, s = {"L$0"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$submitSubscriptionRequest$1.dex */
final class SchoolViewModel$submitSubscriptionRequest$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ String $phoneNumber;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ String $transactionId;
    Object L$0;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$submitSubscriptionRequest$1(String str, String str2, String str3, SchoolViewModel schoolViewModel, int i, Continuation<? super SchoolViewModel$submitSubscriptionRequest$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$phoneNumber = str2;
        this.$transactionId = str3;
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$submitSubscriptionRequest$1(this.$email, this.$phoneNumber, this.$transactionId, this.this$0, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        SchoolRepository schoolRepository;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
                try {
                    Intrinsics.checkNotNull(db.collection("schools").document(this.$email).update(MapsKt.mapOf(new Pair[]{TuplesKt.to("isPendingValidation", Boxing.boxBoolean(true)), TuplesKt.to("paymentPhoneNumber", this.$phoneNumber), TuplesKt.to("transactionId", this.$transactionId), TuplesKt.to("rejectionReason", (Object) null)})));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                schoolRepository = this.this$0.repository;
                this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                this.label = 1;
                if (schoolRepository.submitSubscriptionRequest(this.$schoolId, this.$phoneNumber, this.$transactionId, (Continuation) this) != coroutine_suspended) {
                    break;
                } else {
                    return coroutine_suspended;
                }
            case 1:
                FirebaseFirestore firebaseFirestore = (FirebaseFirestore) this.L$0;
                ResultKt.throwOnFailure($result);
                break;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        return Unit.INSTANCE;
    }
}
