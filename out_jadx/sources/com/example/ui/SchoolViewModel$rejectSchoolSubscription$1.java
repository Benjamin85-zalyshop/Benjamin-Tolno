package com.example.ui;

import com.google.android.gms.tasks.Task;
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
import kotlinx.coroutines.tasks.TasksKt;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$rejectSchoolSubscription$1", f = "SchoolViewModel.kt", i = {0}, l = {2183}, m = "invokeSuspend", n = {"db"}, s = {"L$0"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$rejectSchoolSubscription$1.dex */
final class SchoolViewModel$rejectSchoolSubscription$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ String $reason;
    Object L$0;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$rejectSchoolSubscription$1(String str, String str2, SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$rejectSchoolSubscription$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$reason = str2;
        this.this$0 = schoolViewModel;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$rejectSchoolSubscription$1(this.$email, this.$reason, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        Exception e;
        Task update;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
                try {
                    update = db.collection("schools").document(this.$email).update(MapsKt.mapOf(new Pair[]{TuplesKt.to("hasActiveSubscription", Boxing.boxBoolean(false)), TuplesKt.to("isPendingValidation", Boxing.boxBoolean(false)), TuplesKt.to("rejectionReason", this.$reason)}));
                    Intrinsics.checkNotNullExpressionValue(update, "update(...)");
                    this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                    this.label = 1;
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    return Unit.INSTANCE;
                }
                if (TasksKt.await(update, (Continuation) this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
                this.this$0.loadAdminSchools();
                return Unit.INSTANCE;
            case 1:
                FirebaseFirestore firebaseFirestore = (FirebaseFirestore) this.L$0;
                try {
                    ResultKt.throwOnFailure($result);
                    this.this$0.loadAdminSchools();
                } catch (Exception e3) {
                    e = e3;
                    e.printStackTrace();
                    return Unit.INSTANCE;
                }
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
