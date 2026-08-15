package com.example.ui;

import com.example.data.models.Subject;
import com.example.data.repository.SchoolRepository;
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
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$insertSubject$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0}, l = {1207}, m = "invokeSuspend", n = {"db", "docRef", "remoteId", "data"}, s = {"L$0", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$insertSubject$1.dex */
public final class SchoolViewModel$insertSubject$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ int $coefficient;
    final /* synthetic */ String $email;
    final /* synthetic */ String $grade;
    final /* synthetic */ float $maxScore;
    final /* synthetic */ String $name;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ String $section;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$insertSubject$1(String str, String str2, String str3, String str4, int i, float f, SchoolViewModel schoolViewModel, int i2, Continuation<? super SchoolViewModel$insertSubject$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$section = str2;
        this.$grade = str3;
        this.$name = str4;
        this.$coefficient = i;
        this.$maxScore = f;
        this.this$0 = schoolViewModel;
        this.$schoolId = i2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$insertSubject$1(this.$email, this.$section, this.$grade, this.$name, this.$coefficient, this.$maxScore, this.this$0, this.$schoolId, continuation);
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
                DocumentReference docRef = db.collection("schools").document(this.$email).collection("subjects").document();
                Intrinsics.checkNotNullExpressionValue(docRef, "document(...)");
                String remoteId = docRef.getId();
                Intrinsics.checkNotNullExpressionValue(remoteId, "getId(...)");
                HashMap data = MapsKt.hashMapOf(new Pair[]{TuplesKt.to("section", this.$section), TuplesKt.to("grade", this.$grade), TuplesKt.to("name", this.$name), TuplesKt.to("coefficient", Boxing.boxInt(this.$coefficient)), TuplesKt.to("maxScore", Boxing.boxFloat(this.$maxScore))});
                try {
                    Intrinsics.checkNotNull(docRef.set(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                schoolRepository = this.this$0.repository;
                this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                this.L$1 = SpillingKt.nullOutSpilledVariable(docRef);
                this.L$2 = SpillingKt.nullOutSpilledVariable(remoteId);
                this.L$3 = SpillingKt.nullOutSpilledVariable(data);
                this.label = 1;
                if (schoolRepository.insertSubject(new Subject(0, this.$schoolId, this.$section, this.$grade, this.$name, this.$coefficient, this.$maxScore, remoteId, 1, (DefaultConstructorMarker) null), (Continuation) this) != coroutine_suspended) {
                    break;
                } else {
                    return coroutine_suspended;
                }
            case 1:
                HashMap hashMap = (HashMap) this.L$3;
                String str = (String) this.L$2;
                DocumentReference documentReference = (DocumentReference) this.L$1;
                FirebaseFirestore firebaseFirestore = (FirebaseFirestore) this.L$0;
                ResultKt.throwOnFailure($result);
                break;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        return Unit.INSTANCE;
    }
}
