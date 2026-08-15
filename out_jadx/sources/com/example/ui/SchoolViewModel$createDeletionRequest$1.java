package com.example.ui;

import com.example.data.models.Student;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
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
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$createDeletionRequest$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$createDeletionRequest$1.dex */
final class SchoolViewModel$createDeletionRequest$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ String $reason;
    final /* synthetic */ Student $student;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$createDeletionRequest$1(String str, Student student, String str2, Continuation<? super SchoolViewModel$createDeletionRequest$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$student = student;
        this.$reason = str2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$createDeletionRequest$1(this.$email, this.$student, this.$reason, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
                DocumentReference docRef = db.collection("schools").document(this.$email).collection("deletionRequests").document();
                Intrinsics.checkNotNullExpressionValue(docRef, "document(...)");
                HashMap requestData = MapsKt.hashMapOf(new Pair[]{TuplesKt.to("studentRemoteId", this.$student.getRemoteId()), TuplesKt.to("studentName", this.$student.getFirstName() + " " + this.$student.getLastName()), TuplesKt.to("grade", this.$student.getGrade()), TuplesKt.to("section", this.$student.getSection()), TuplesKt.to("reason", this.$reason), TuplesKt.to("requestedBy", "Financier"), TuplesKt.to("requestedAt", Boxing.boxLong(System.currentTimeMillis())), TuplesKt.to("status", "PENDING"), TuplesKt.to("rejectionReason", "")});
                try {
                    Intrinsics.checkNotNull(docRef.set(requestData));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
