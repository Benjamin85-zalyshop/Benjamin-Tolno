package com.example.ui;

import com.google.firebase.firestore.DocumentChange;
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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$expensesListener$1$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3}, l = {957, 959, 974, 986}, m = "invokeSuspend", n = {"change", "doc", "remoteId", "reason", "section", "amount", "date", "change", "doc", "remoteId", "reason", "section", "existingExpense", "amount", "date", "change", "doc", "remoteId", "reason", "section", "existingExpense", "amount", "date", "change", "doc", "remoteId"}, s = {"L$1", "L$2", "L$3", "L$4", "L$5", "J$0", "J$1", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "J$0", "J$1", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "J$0", "J$1", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$expensesListener$1$1.dex */
public final class SchoolViewModel$startRealtimeSync$expensesListener$1$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ int $schoolId;
    final /* synthetic */ QuerySnapshot $snapshot;
    long J$0;
    long J$1;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* compiled from: SchoolViewModel.kt */
    @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
    /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$expensesListener$1$1$WhenMappings.dex */
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
    public SchoolViewModel$startRealtimeSync$expensesListener$1$1(QuerySnapshot querySnapshot, SchoolViewModel schoolViewModel, int i, Continuation<? super SchoolViewModel$startRealtimeSync$expensesListener$1$1> continuation) {
        super(2, continuation);
        this.$snapshot = querySnapshot;
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$startRealtimeSync$expensesListener$1$1(this.$snapshot, this.this$0, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x00d0  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x01a8  */
    /* JADX WARN: Removed duplicated region for block: B:47:0x0235  */
    /* JADX WARN: Removed duplicated region for block: B:65:0x02fa  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:20:0x012b -> B:21:0x0133). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:46:0x022e -> B:11:0x00ca). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:56:0x0267 -> B:11:0x00ca). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:64:0x02f4 -> B:11:0x00ca). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r34) {
        /*
            Method dump skipped, instructions count: 790
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$startRealtimeSync$expensesListener$1$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
