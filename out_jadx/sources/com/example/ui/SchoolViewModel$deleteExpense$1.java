package com.example.ui;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.CoroutineScope;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$deleteExpense$1", f = "SchoolViewModel.kt", i = {1, 1}, l = {656, 667}, m = "invokeSuspend", n = {"expensesList", "expense"}, s = {"L$0", "L$1"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$deleteExpense$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n1#2:2220\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$deleteExpense$1.dex */
final class SchoolViewModel$deleteExpense$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ int $expenseId;
    Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$deleteExpense$1(SchoolViewModel schoolViewModel, String str, int i, Continuation<? super SchoolViewModel$deleteExpense$1> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$email = str;
        this.$expenseId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$deleteExpense$1(this.this$0, this.$email, this.$expenseId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x005f  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0079  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00db A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:40:0x00dc  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x0073 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r11) {
        /*
            r10 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r10.label
            r2 = 1
            switch(r1) {
                case 0: goto L24;
                case 1: goto L1f;
                case 2: goto L12;
                default: goto La;
            }
        La:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L12:
            java.lang.Object r0 = r10.L$1
            com.example.data.models.Expense r0 = (com.example.data.models.Expense) r0
            java.lang.Object r1 = r10.L$0
            java.util.List r1 = (java.util.List) r1
            kotlin.ResultKt.throwOnFailure(r11)
            goto Ldd
        L1f:
            kotlin.ResultKt.throwOnFailure(r11)
            r1 = r11
            goto L4d
        L24:
            kotlin.ResultKt.throwOnFailure(r11)
            com.example.ui.SchoolViewModel r1 = r10.this$0
            com.example.data.repository.SchoolRepository r1 = com.example.ui.SchoolViewModel.access$getRepository$p(r1)
            com.example.ui.SchoolViewModel r3 = r10.this$0
            kotlinx.coroutines.flow.MutableStateFlow r3 = com.example.ui.SchoolViewModel.access$get_currentSchoolId$p(r3)
            java.lang.Object r3 = r3.getValue()
            java.lang.Integer r3 = (java.lang.Integer) r3
            if (r3 == 0) goto L40
            int r3 = r3.intValue()
            goto L41
        L40:
            r3 = -1
        L41:
            r4 = r10
            kotlin.coroutines.Continuation r4 = (kotlin.coroutines.Continuation) r4
            r10.label = r2
            java.lang.Object r1 = r1.getAllExpensesDirect(r3, r4)
            if (r1 != r0) goto L4d
            return r0
        L4d:
            java.util.List r1 = (java.util.List) r1
            r3 = r1
            java.lang.Iterable r3 = (java.lang.Iterable) r3
            int r4 = r10.$expenseId
            java.util.Iterator r3 = r3.iterator()
        L58:
            boolean r5 = r3.hasNext()
            r6 = 0
            if (r5 == 0) goto L73
            java.lang.Object r5 = r3.next()
            r7 = r5
            com.example.data.models.Expense r7 = (com.example.data.models.Expense) r7
            r8 = 0
            int r9 = r7.getId()
            if (r9 != r4) goto L6f
            r7 = r2
            goto L70
        L6f:
            r7 = r6
        L70:
            if (r7 == 0) goto L58
            goto L74
        L73:
            r5 = 0
        L74:
            r3 = r5
            com.example.data.models.Expense r3 = (com.example.data.models.Expense) r3
            if (r3 == 0) goto Lbb
            java.lang.String r4 = r3.getRemoteId()
            java.lang.CharSequence r4 = (java.lang.CharSequence) r4
            int r4 = r4.length()
            if (r4 <= 0) goto L86
            goto L87
        L86:
            r2 = r6
        L87:
            if (r2 == 0) goto Lbb
            com.google.firebase.firestore.FirebaseFirestore r2 = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            java.lang.String r4 = "getInstance(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r2, r4)
            java.lang.String r4 = "schools"
            com.google.firebase.firestore.CollectionReference r4 = r2.collection(r4)     // Catch: java.lang.Exception -> Lb5
            java.lang.String r5 = r10.$email     // Catch: java.lang.Exception -> Lb5
            com.google.firebase.firestore.DocumentReference r4 = r4.document(r5)     // Catch: java.lang.Exception -> Lb5
            java.lang.String r5 = "expenses"
            com.google.firebase.firestore.CollectionReference r4 = r4.collection(r5)     // Catch: java.lang.Exception -> Lb5
            java.lang.String r5 = r3.getRemoteId()     // Catch: java.lang.Exception -> Lb5
            com.google.firebase.firestore.DocumentReference r4 = r4.document(r5)     // Catch: java.lang.Exception -> Lb5
            com.google.android.gms.tasks.Task r4 = r4.delete()     // Catch: java.lang.Exception -> Lb5
            kotlin.jvm.internal.Intrinsics.checkNotNull(r4)     // Catch: java.lang.Exception -> Lb5
            goto Lbb
        Lb5:
            r4 = move-exception
            r4.printStackTrace()
            kotlin.Unit r5 = kotlin.Unit.INSTANCE
        Lbb:
            com.example.ui.SchoolViewModel r2 = r10.this$0
            com.example.data.repository.SchoolRepository r2 = com.example.ui.SchoolViewModel.access$getRepository$p(r2)
            int r4 = r10.$expenseId
            r5 = r10
            kotlin.coroutines.Continuation r5 = (kotlin.coroutines.Continuation) r5
            java.lang.Object r6 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r1)
            r10.L$0 = r6
            java.lang.Object r6 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r3)
            r10.L$1 = r6
            r6 = 2
            r10.label = r6
            java.lang.Object r2 = r2.deleteExpense(r4, r5)
            if (r2 != r0) goto Ldc
            return r0
        Ldc:
            r0 = r3
        Ldd:
            kotlin.Unit r2 = kotlin.Unit.INSTANCE
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$deleteExpense$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
