package com.example.ui;

import com.example.data.models.Student;
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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$insertStudent$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0}, l = {335}, m = "invokeSuspend", n = {"db", "docRef", "remoteId", "studentData"}, s = {"L$0", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$insertStudent$1.dex */
public final class SchoolViewModel$insertStudent$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ String $firstName;
    final /* synthetic */ String $grade;
    final /* synthetic */ String $lastName;
    final /* synthetic */ String $parentWhatsApp;
    final /* synthetic */ String $photoBase64;
    final /* synthetic */ long $reenrollmentFee;
    final /* synthetic */ long $registrationFee;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ String $section;
    final /* synthetic */ String $year;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$insertStudent$1(String str, String str2, String str3, String str4, String str5, String str6, long j, long j2, String str7, String str8, SchoolViewModel schoolViewModel, int i, Continuation<? super SchoolViewModel$insertStudent$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$firstName = str2;
        this.$lastName = str3;
        this.$grade = str4;
        this.$section = str5;
        this.$parentWhatsApp = str6;
        this.$registrationFee = j;
        this.$reenrollmentFee = j2;
        this.$photoBase64 = str7;
        this.$year = str8;
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$insertStudent$1(this.$email, this.$firstName, this.$lastName, this.$grade, this.$section, this.$parentWhatsApp, this.$registrationFee, this.$reenrollmentFee, this.$photoBase64, this.$year, this.this$0, this.$schoolId, continuation);
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
                DocumentReference docRef = db.collection("schools").document(this.$email).collection("students").document();
                Intrinsics.checkNotNullExpressionValue(docRef, "document(...)");
                String remoteId = docRef.getId();
                Intrinsics.checkNotNullExpressionValue(remoteId, "getId(...)");
                Pair[] pairArr = new Pair[9];
                pairArr[0] = TuplesKt.to("firstName", this.$firstName);
                pairArr[1] = TuplesKt.to("lastName", this.$lastName);
                pairArr[2] = TuplesKt.to("grade", this.$grade);
                pairArr[3] = TuplesKt.to("section", this.$section);
                String str = this.$parentWhatsApp;
                if (str == null) {
                    str = "";
                }
                pairArr[4] = TuplesKt.to("parentWhatsApp", str);
                pairArr[5] = TuplesKt.to("registrationFee", Boxing.boxLong(this.$registrationFee));
                pairArr[6] = TuplesKt.to("reenrollmentFee", Boxing.boxLong(this.$reenrollmentFee));
                String str2 = this.$photoBase64;
                pairArr[7] = TuplesKt.to("photoBase64", str2 != null ? str2 : "");
                pairArr[8] = TuplesKt.to("schoolYear", this.$year);
                HashMap studentData = MapsKt.hashMapOf(pairArr);
                try {
                    Intrinsics.checkNotNull(docRef.set(studentData));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                schoolRepository = this.this$0.repository;
                this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                this.L$1 = SpillingKt.nullOutSpilledVariable(docRef);
                this.L$2 = SpillingKt.nullOutSpilledVariable(remoteId);
                this.L$3 = SpillingKt.nullOutSpilledVariable(studentData);
                this.label = 1;
                if (schoolRepository.insertStudent(new Student(0, this.$schoolId, this.$firstName, this.$lastName, this.$grade, this.$section, remoteId, this.$parentWhatsApp, this.$registrationFee, this.$reenrollmentFee, this.$photoBase64, this.$year, 1, (DefaultConstructorMarker) null), (Continuation) this) != coroutine_suspended) {
                    break;
                } else {
                    return coroutine_suspended;
                }
            case 1:
                HashMap hashMap = (HashMap) this.L$3;
                String str3 = (String) this.L$2;
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
