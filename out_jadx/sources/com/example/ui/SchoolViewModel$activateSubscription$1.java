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
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$activateSubscription$1", f = "SchoolViewModel.kt", i = {0, 0}, l = {1992}, m = "invokeSuspend", n = {"db", "newExpiryDate"}, s = {"L$0", "J$0"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$activateSubscription$1.dex */
public final class SchoolViewModel$activateSubscription$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ int $schoolId;
    long J$0;
    Object L$0;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$activateSubscription$1(String str, SchoolViewModel schoolViewModel, int i, Continuation<? super SchoolViewModel$activateSubscription$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$activateSubscription$1(this.$email, this.this$0, this.$schoolId, continuation);
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
                long newExpiryDate = System.currentTimeMillis() + 31536000000L;
                try {
                    Intrinsics.checkNotNull(db.collection("schools").document(this.$email).update(MapsKt.mapOf(new Pair[]{TuplesKt.to("hasActiveSubscription", Boxing.boxBoolean(true)), TuplesKt.to("subscriptionExpiryDate", Boxing.boxLong(newExpiryDate)), TuplesKt.to("isPendingValidation", Boxing.boxBoolean(false))})));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                schoolRepository = this.this$0.repository;
                this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                this.J$0 = newExpiryDate;
                this.label = 1;
                if (schoolRepository.activateSubscription(this.$schoolId, newExpiryDate, (Continuation) this) != coroutine_suspended) {
                    break;
                } else {
                    return coroutine_suspended;
                }
            case 1:
                long j = this.J$0;
                FirebaseFirestore firebaseFirestore = (FirebaseFirestore) this.L$0;
                ResultKt.throwOnFailure($result);
                break;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        return Unit.INSTANCE;
    }
}
