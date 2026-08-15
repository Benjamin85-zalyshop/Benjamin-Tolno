package com.example.ui;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$studentsListener$1$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3}, l = {789, 791, 816, 833}, m = "invokeSuspend", n = {"change", "doc", "remoteId", "firstName", "lastName", "grade", "section", "parentWhatsApp", "photoBase64", "schoolYear", "normalizedGrade", "primaryGrades", "regFee", "reenrFee", "needsUpdate", "change", "doc", "remoteId", "firstName", "lastName", "grade", "section", "parentWhatsApp", "photoBase64", "schoolYear", "normalizedGrade", "primaryGrades", "existingStudent", "regFee", "reenrFee", "needsUpdate", "change", "doc", "remoteId", "firstName", "lastName", "grade", "section", "parentWhatsApp", "photoBase64", "schoolYear", "normalizedGrade", "primaryGrades", "existingStudent", "regFee", "reenrFee", "needsUpdate", "change", "doc", "remoteId"}, s = {"L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "J$0", "J$1", "I$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "J$0", "J$1", "I$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "J$0", "J$1", "I$0", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$studentsListener$1$1.dex */
public final class SchoolViewModel$startRealtimeSync$studentsListener$1$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ int $schoolId;
    final /* synthetic */ QuerySnapshot $snapshot;
    int I$0;
    long J$0;
    long J$1;
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
    /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$studentsListener$1$1$WhenMappings.dex */
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
    public SchoolViewModel$startRealtimeSync$studentsListener$1$1(QuerySnapshot querySnapshot, SchoolViewModel schoolViewModel, int i, Continuation<? super SchoolViewModel$startRealtimeSync$studentsListener$1$1> continuation) {
        super(2, continuation);
        this.$snapshot = querySnapshot;
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$startRealtimeSync$studentsListener$1$1(this.$snapshot, this.this$0, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:101:0x05b0  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x015f  */
    /* JADX WARN: Removed duplicated region for block: B:63:0x031f  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x0406  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:100:0x05a6 -> B:11:0x0159). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:20:0x01c8 -> B:21:0x01d0). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:67:0x03fc -> B:11:0x0159). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:87:0x0483 -> B:11:0x0159). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r51) {
        /*
            Method dump skipped, instructions count: 1484
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$startRealtimeSync$studentsListener$1$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: SchoolViewModel.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
    @DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$studentsListener$1$1$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.example.ui.SchoolViewModel$startRealtimeSync$studentsListener$1$1$1  reason: invalid class name */
    /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$studentsListener$1$1$1.dex */
    public static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ QueryDocumentSnapshot $doc;
        final /* synthetic */ String $finalGrade;
        final /* synthetic */ String $finalSection;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(QueryDocumentSnapshot queryDocumentSnapshot, String str, String str2, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.$doc = queryDocumentSnapshot;
            this.$finalGrade = str;
            this.$finalSection = str2;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass1(this.$doc, this.$finalGrade, this.$finalSection, continuation);
        }

        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object $result) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    try {
                        Intrinsics.checkNotNull(this.$doc.getReference().update(MapsKt.mapOf(new Pair[]{TuplesKt.to("grade", this.$finalGrade), TuplesKt.to("section", this.$finalSection)})));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }
}
