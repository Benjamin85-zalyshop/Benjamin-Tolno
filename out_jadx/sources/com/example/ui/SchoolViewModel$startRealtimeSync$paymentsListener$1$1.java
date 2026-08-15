package com.example.ui;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$paymentsListener$1$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7}, l = {863, 866, 875, 888, 895, 897, 914, 928}, m = "invokeSuspend", n = {"change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "amount", "date", "change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "localStudentId", "amount", "date", "change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "localStudentId", "stuDoc", "firstName", "lastName", "grade", "section", "parentWhatsApp", "amount", "date", "regFee", "reenrFee", "change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "localStudentId", "stuDoc", "firstName", "lastName", "grade", "section", "parentWhatsApp", "amount", "date", "regFee", "reenrFee", "change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "localStudentId", "amount", "date", "change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "localStudentId", "existingPayment", "amount", "date", "change", "doc", "remoteId", "studentRemoteId", "reason", "paymentMethod", "localStudentId", "existingPayment", "amount", "date", "change", "doc", "remoteId"}, s = {"L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "J$0", "J$1", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "J$0", "J$1", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "J$0", "J$1", "J$2", "J$3", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "J$0", "J$1", "J$2", "J$3", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "J$0", "J$1", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "J$0", "J$1", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "J$0", "J$1", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$paymentsListener$1$1.dex */
public final class SchoolViewModel$startRealtimeSync$paymentsListener$1$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ DocumentReference $schoolDocRef;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ QuerySnapshot $snapshot;
    long J$0;
    long J$1;
    long J$2;
    long J$3;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* compiled from: SchoolViewModel.kt */
    @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
    /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$paymentsListener$1$1$WhenMappings.dex */
    public static final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[DocumentChange.Type.values().length];
            try {
                iArr[DocumentChange.Type.ADDED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DocumentChange.Type.MODIFIED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DocumentChange.Type.REMOVED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$startRealtimeSync$paymentsListener$1$1(QuerySnapshot querySnapshot, SchoolViewModel schoolViewModel, DocumentReference documentReference, int i, Continuation<? super SchoolViewModel$startRealtimeSync$paymentsListener$1$1> continuation) {
        super(2, continuation);
        this.$snapshot = querySnapshot;
        this.this$0 = schoolViewModel;
        this.$schoolDocRef = documentReference;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$startRealtimeSync$paymentsListener$1$1(this.$snapshot, this.this$0, this.$schoolDocRef, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(14:91|(1:92)|93|94|95|96|97|98|99|100|101|102|103|(1:105)(5:106|107|108|109|(2:111|(1:113)(3:114|115|(2:117|(1:119)(4:120|121|9|(2:193|194)(0)))(4:122|(2:128|(3:134|9|(0)(0)))|135|(1:137)(4:138|139|9|(0)(0)))))(3:140|9|(0)(0)))) */
    /* JADX WARN: Can't wrap try/catch for region: R(14:91|92|93|94|95|96|97|98|99|100|101|102|103|(1:105)(5:106|107|108|109|(2:111|(1:113)(3:114|115|(2:117|(1:119)(4:120|121|9|(2:193|194)(0)))(4:122|(2:128|(3:134|9|(0)(0)))|135|(1:137)(4:138|139|9|(0)(0)))))(3:140|9|(0)(0)))) */
    /* JADX WARN: Can't wrap try/catch for region: R(31:52|(1:53)|54|(1:56)(1:178)|57|(1:59)(1:177)|60|(1:62)(1:176)|63|(1:65)|66|67|(1:69)(1:175)|70|(2:170|171)(1:72)|73|74|(1:76)(1:169)|77|78|79|80|81|82|83|84|85|86|87|88|(1:90)(14:91|92|93|94|95|96|97|98|99|100|101|102|103|(1:105)(5:106|107|108|109|(2:111|(1:113)(3:114|115|(2:117|(1:119)(4:120|121|9|(2:193|194)(0)))(4:122|(2:128|(3:134|9|(0)(0)))|135|(1:137)(4:138|139|9|(0)(0)))))(3:140|9|(0)(0))))) */
    /* JADX WARN: Can't wrap try/catch for region: R(31:52|53|54|(1:56)(1:178)|57|(1:59)(1:177)|60|(1:62)(1:176)|63|(1:65)|66|67|(1:69)(1:175)|70|(2:170|171)(1:72)|73|74|(1:76)(1:169)|77|78|79|80|81|82|83|84|85|86|87|88|(1:90)(14:91|92|93|94|95|96|97|98|99|100|101|102|103|(1:105)(5:106|107|108|109|(2:111|(1:113)(3:114|115|(2:117|(1:119)(4:120|121|9|(2:193|194)(0)))(4:122|(2:128|(3:134|9|(0)(0)))|135|(1:137)(4:138|139|9|(0)(0)))))(3:140|9|(0)(0))))) */
    /* JADX WARN: Code restructure failed: missing block: B:130:0x058c, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:131:0x058d, code lost:
        r21 = r1;
        r20 = r2;
        r22 = r4;
        r23 = r5;
        r18 = r6;
        r2 = r17;
        r25 = r27;
        r19 = r28;
        r1 = r45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:132:0x05a2, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:133:0x05a3, code lost:
        r21 = r18;
        r20 = r2;
        r22 = r4;
        r23 = r5;
        r18 = r6;
        r2 = r17;
        r25 = r27;
        r19 = r28;
        r1 = r45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:134:0x05ba, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:135:0x05bb, code lost:
        r28 = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:138:0x05c3, code lost:
        r1 = r18;
        r21 = r1;
        r20 = r2;
        r22 = r4;
        r23 = r5;
        r18 = r6;
        r2 = r17;
        r25 = r27;
        r19 = r28;
        r1 = r45;
     */
    /* JADX WARN: Code restructure failed: missing block: B:139:0x05da, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:140:0x05db, code lost:
        r2 = r38;
        r1 = r45;
        r23 = r6;
        r22 = r7;
        r24 = r13;
        r20 = r14;
        r19 = r15;
        r25 = r17;
        r21 = r18;
        r18 = r8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:141:0x05f1, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:142:0x05f2, code lost:
        r18 = r4;
        r14 = r5;
        r2 = r38;
     */
    /* JADX WARN: Code restructure failed: missing block: B:143:0x05f8, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:144:0x05f9, code lost:
        r18 = r4;
        r17 = r14;
        r2 = r38;
     */
    /* JADX WARN: Code restructure failed: missing block: B:147:0x0605, code lost:
        r14 = r5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:148:0x0606, code lost:
        r1 = r45;
        r23 = r6;
        r22 = r7;
        r24 = r13;
        r20 = r14;
        r19 = r15;
        r25 = r17;
        r21 = r18;
        r18 = r8;
     */
    /* JADX WARN: Removed duplicated region for block: B:124:0x0557 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:125:0x0558  */
    /* JADX WARN: Removed duplicated region for block: B:149:0x0619  */
    /* JADX WARN: Removed duplicated region for block: B:157:0x0665  */
    /* JADX WARN: Removed duplicated region for block: B:163:0x06b6  */
    /* JADX WARN: Removed duplicated region for block: B:168:0x0749  */
    /* JADX WARN: Removed duplicated region for block: B:188:0x080d  */
    /* JADX WARN: Removed duplicated region for block: B:189:0x0814  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x0263  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x036e  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x03c8  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:167:0x0740 -> B:23:0x025d). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:182:0x077f -> B:23:0x025d). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:187:0x0803 -> B:23:0x025d). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:188:0x080d -> B:23:0x025d). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:32:0x02d1 -> B:33:0x02d8). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r45) {
        /*
            Method dump skipped, instructions count: 2106
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$startRealtimeSync$paymentsListener$1$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
