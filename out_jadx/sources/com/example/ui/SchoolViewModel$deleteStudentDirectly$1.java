package com.example.ui;

import com.example.data.models.Payment;
import com.example.data.models.Student;
import com.example.data.repository.SchoolRepository;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.MutableStateFlow;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$deleteStudentDirectly$1", f = "SchoolViewModel.kt", i = {0, 0, 0}, l = {541}, m = "invokeSuspend", n = {"db", "schoolRef", "schoolId"}, s = {"L$0", "L$1", "I$0"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$deleteStudentDirectly$1\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,2219:1\n774#2:2220\n865#2,2:2221\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$deleteStudentDirectly$1\n*L\n542#1:2220\n542#1:2221,2\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$deleteStudentDirectly$1.dex */
final class SchoolViewModel$deleteStudentDirectly$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ Student $student;
    int I$0;
    Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$deleteStudentDirectly$1(String str, Student student, SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$deleteStudentDirectly$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$student = student;
        this.this$0 = schoolViewModel;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$deleteStudentDirectly$1(this.$email, this.$student, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        DocumentReference schoolRef;
        MutableStateFlow mutableStateFlow;
        SchoolRepository schoolRepository;
        Object allPaymentsDirect;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        try {
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
                    schoolRef = db.collection("schools").document(this.$email);
                    Intrinsics.checkNotNullExpressionValue(schoolRef, "document(...)");
                    schoolRef.collection("students").document(this.$student.getRemoteId()).delete();
                    mutableStateFlow = this.this$0._currentSchoolId;
                    Integer num = (Integer) mutableStateFlow.getValue();
                    int schoolId = num != null ? num.intValue() : -1;
                    schoolRepository = this.this$0.repository;
                    this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                    this.L$1 = schoolRef;
                    this.I$0 = schoolId;
                    this.label = 1;
                    allPaymentsDirect = schoolRepository.getAllPaymentsDirect(schoolId, (Continuation) this);
                    if (allPaymentsDirect != coroutine_suspended) {
                        break;
                    } else {
                        return coroutine_suspended;
                    }
                case 1:
                    int i = this.I$0;
                    schoolRef = (DocumentReference) this.L$1;
                    FirebaseFirestore firebaseFirestore = (FirebaseFirestore) this.L$0;
                    ResultKt.throwOnFailure($result);
                    allPaymentsDirect = $result;
                    break;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            Iterable allLocalPayments = (List) allPaymentsDirect;
            Iterable $this$filter$iv = allLocalPayments;
            Student student = this.$student;
            Collection destination$iv$iv = new ArrayList();
            for (Object element$iv$iv : $this$filter$iv) {
                Payment it = (Payment) element$iv$iv;
                if (it.getStudentId() == student.getId()) {
                    destination$iv$iv.add(element$iv$iv);
                }
            }
            List<Payment> studentPayments = (List) destination$iv$iv;
            for (Payment payment : studentPayments) {
                if (payment.getRemoteId().length() > 0) {
                    schoolRef.collection("payments").document(payment.getRemoteId()).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Unit.INSTANCE;
    }
}
