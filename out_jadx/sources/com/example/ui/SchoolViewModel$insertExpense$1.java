package com.example.ui;

import com.example.data.models.Expense;
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
import kotlinx.coroutines.flow.MutableStateFlow;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$insertExpense$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {640}, m = "invokeSuspend", n = {"db", "docRef", "remoteId", "expenseData", "date"}, s = {"L$0", "L$1", "L$2", "L$3", "J$0"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$insertExpense$1.dex */
final class SchoolViewModel$insertExpense$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ long $amount;
    final /* synthetic */ String $description;
    final /* synthetic */ String $email;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ String $section;
    long J$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$insertExpense$1(String str, SchoolViewModel schoolViewModel, long j, String str2, String str3, int i, Continuation<? super SchoolViewModel$insertExpense$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.this$0 = schoolViewModel;
        this.$amount = j;
        this.$section = str2;
        this.$description = str3;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$insertExpense$1(this.$email, this.this$0, this.$amount, this.$section, this.$description, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        MutableStateFlow mutableStateFlow;
        long date;
        SchoolRepository schoolRepository;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
                DocumentReference docRef = db.collection("schools").document(this.$email).collection("expenses").document();
                Intrinsics.checkNotNullExpressionValue(docRef, "document(...)");
                String remoteId = docRef.getId();
                Intrinsics.checkNotNullExpressionValue(remoteId, "getId(...)");
                SchoolViewModel schoolViewModel = this.this$0;
                long currentTimeMillis = System.currentTimeMillis();
                mutableStateFlow = this.this$0._selectedSchoolYear;
                date = schoolViewModel.adjustTimestampToSchoolYear(currentTimeMillis, (String) mutableStateFlow.getValue());
                HashMap expenseData = MapsKt.hashMapOf(new Pair[]{TuplesKt.to("amount", Boxing.boxLong(this.$amount)), TuplesKt.to("section", this.$section), TuplesKt.to("reason", this.$description), TuplesKt.to("date", Boxing.boxLong(date))});
                try {
                    Intrinsics.checkNotNull(docRef.set(expenseData));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                schoolRepository = this.this$0.repository;
                this.L$0 = SpillingKt.nullOutSpilledVariable(db);
                this.L$1 = SpillingKt.nullOutSpilledVariable(docRef);
                this.L$2 = SpillingKt.nullOutSpilledVariable(remoteId);
                this.L$3 = SpillingKt.nullOutSpilledVariable(expenseData);
                this.J$0 = date;
                this.label = 1;
                if (schoolRepository.insertExpense(new Expense(0, this.$schoolId, this.$amount, date, this.$description, this.$section, remoteId, 1, (DefaultConstructorMarker) null), (Continuation) this) != coroutine_suspended) {
                    break;
                } else {
                    return coroutine_suspended;
                }
            case 1:
                long j = this.J$0;
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
