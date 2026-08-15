package com.example.ui;

import com.example.data.models.Expense;
import com.example.data.repository.SchoolRepository;
import java.util.List;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\u001c\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\u0010\u0000\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u00020\u00012\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007H\n"}, d2 = {"<anonymous>", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/data/models/Expense;", "id", "", "section", "", "schoolYear"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$expenses$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$expenses$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt\n+ 4 SafeCollector.common.kt\nkotlinx/coroutines/flow/internal/SafeCollector_commonKt\n*L\n1#1,2219:1\n49#2:2220\n51#2:2224\n49#2:2225\n51#2:2229\n46#3:2221\n51#3:2223\n46#3:2226\n51#3:2228\n105#4:2222\n105#4:2227\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$expenses$1\n*L\n246#1:2220\n246#1:2224\n247#1:2225\n247#1:2229\n246#1:2221\n246#1:2223\n247#1:2226\n247#1:2228\n246#1:2222\n247#1:2227\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$expenses$1.dex */
public final class SchoolViewModel$expenses$1 extends SuspendLambda implements Function4<Integer, String, String, Continuation<? super Flow<? extends List<? extends Expense>>>, Object> {
    /* synthetic */ Object L$0;
    /* synthetic */ Object L$1;
    /* synthetic */ Object L$2;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$expenses$1(SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$expenses$1> continuation) {
        super(4, continuation);
        this.this$0 = schoolViewModel;
    }

    public final Object invoke(Integer num, String str, String str2, Continuation<? super Flow<? extends List<Expense>>> continuation) {
        SchoolViewModel$expenses$1 schoolViewModel$expenses$1 = new SchoolViewModel$expenses$1(this.this$0, continuation);
        schoolViewModel$expenses$1.L$0 = num;
        schoolViewModel$expenses$1.L$1 = str;
        schoolViewModel$expenses$1.L$2 = str2;
        return schoolViewModel$expenses$1.invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        SchoolRepository schoolRepository;
        Flow $this$map$iv;
        SchoolRepository schoolRepository2;
        Integer id = (Integer) this.L$0;
        final String section = (String) this.L$1;
        final String schoolYear = (String) this.L$2;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                if (id != null) {
                    if (Intrinsics.areEqual(section, "Toutes les sections")) {
                        schoolRepository2 = this.this$0.repository;
                        $this$map$iv = schoolRepository2.getAllExpenses(id.intValue());
                    } else {
                        schoolRepository = this.this$0.repository;
                        final Flow $this$map$iv2 = schoolRepository.getAllExpenses(id.intValue());
                        $this$map$iv = new Flow<List<? extends Expense>>() { // from class: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1
                            public Object collect(FlowCollector collector, Continuation $completion) {
                                Object collect = $this$map$iv2.collect(new AnonymousClass2(collector, section), $completion);
                                return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
                            }

                            /* compiled from: Emitters.kt */
                            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                            @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$expenses$1\n+ 4 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,49:1\n50#2:50\n246#3:51\n774#4:52\n865#4,2:53\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$expenses$1\n*L\n246#1:52\n246#1:53,2\n*E\n"})
                            /* renamed from: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2  reason: invalid class name */
                            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2.dex */
                            public static final class AnonymousClass2<T> implements FlowCollector {
                                final /* synthetic */ String $section$inlined;
                                final /* synthetic */ FlowCollector $this_unsafeFlow;

                                @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                                @DebugMetadata(c = "com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                                /* renamed from: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2$1  reason: invalid class name */
                                /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2$1.dex */
                                public static final class AnonymousClass1 extends ContinuationImpl {
                                    int I$0;
                                    Object L$0;
                                    Object L$1;
                                    Object L$2;
                                    Object L$3;
                                    int label;
                                    /* synthetic */ Object result;

                                    public AnonymousClass1(Continuation continuation) {
                                        super(continuation);
                                    }

                                    public final Object invokeSuspend(Object obj) {
                                        this.result = obj;
                                        this.label |= Integer.MIN_VALUE;
                                        return AnonymousClass2.this.emit(null, (Continuation) this);
                                    }
                                }

                                public AnonymousClass2(FlowCollector flowCollector, String str) {
                                    this.$this_unsafeFlow = flowCollector;
                                    this.$section$inlined = str;
                                }

                                /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                                /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                                /* JADX WARN: Removed duplicated region for block: B:13:0x0045  */
                                /*
                                    Code decompiled incorrectly, please refer to instructions dump.
                                    To view partially-correct add '--show-bad-code' argument
                                */
                                public final java.lang.Object emit(java.lang.Object r23, kotlin.coroutines.Continuation r24) {
                                    /*
                                        r22 = this;
                                        r0 = r22
                                        r1 = r24
                                        boolean r2 = r1 instanceof com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1.AnonymousClass2.AnonymousClass1
                                        if (r2 == 0) goto L18
                                        r2 = r1
                                        com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2$1 r2 = (com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1.AnonymousClass2.AnonymousClass1) r2
                                        int r3 = r2.label
                                        r4 = -2147483648(0xffffffff80000000, float:-0.0)
                                        r3 = r3 & r4
                                        if (r3 == 0) goto L18
                                        int r3 = r2.label
                                        int r3 = r3 - r4
                                        r2.label = r3
                                        goto L1d
                                    L18:
                                        com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2$1 r2 = new com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2$1
                                        r2.<init>(r1)
                                    L1d:
                                        java.lang.Object r3 = r2.result
                                        java.lang.Object r4 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                                        int r5 = r2.label
                                        switch(r5) {
                                            case 0: goto L45;
                                            case 1: goto L30;
                                            default: goto L28;
                                        }
                                    L28:
                                        java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
                                        java.lang.String r2 = "call to 'resume' before 'invoke' with coroutine"
                                        r1.<init>(r2)
                                        throw r1
                                    L30:
                                        int r4 = r2.I$0
                                        java.lang.Object r5 = r2.L$3
                                        kotlinx.coroutines.flow.FlowCollector r5 = (kotlinx.coroutines.flow.FlowCollector) r5
                                        java.lang.Object r6 = r2.L$2
                                        java.lang.Object r7 = r2.L$1
                                        com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1$2$1 r7 = (com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1.AnonymousClass2.AnonymousClass1) r7
                                        java.lang.Object r8 = r2.L$0
                                        kotlin.ResultKt.throwOnFailure(r3)
                                        r20 = r3
                                        goto Lc1
                                    L45:
                                        kotlin.ResultKt.throwOnFailure(r3)
                                        kotlinx.coroutines.flow.FlowCollector r5 = r0.$this_unsafeFlow
                                        r7 = r2
                                        r6 = r23
                                        r8 = 0
                                        r9 = r2
                                        kotlin.coroutines.Continuation r9 = (kotlin.coroutines.Continuation) r9
                                        r10 = r6
                                        java.util.List r10 = (java.util.List) r10
                                        r11 = 0
                                        r12 = r10
                                        java.lang.Iterable r12 = (java.lang.Iterable) r12
                                        r13 = 0
                                        java.util.ArrayList r14 = new java.util.ArrayList
                                        r14.<init>()
                                        java.util.Collection r14 = (java.util.Collection) r14
                                        r15 = r12
                                        r16 = 0
                                        java.util.Iterator r17 = r15.iterator()
                                    L67:
                                        boolean r18 = r17.hasNext()
                                        if (r18 == 0) goto L91
                                        java.lang.Object r1 = r17.next()
                                        r18 = r1
                                        com.example.data.models.Expense r18 = (com.example.data.models.Expense) r18
                                        r19 = 0
                                        r20 = r3
                                        java.lang.String r3 = r18.getSection()
                                        r21 = r9
                                        java.lang.String r9 = r0.$section$inlined
                                        boolean r3 = kotlin.jvm.internal.Intrinsics.areEqual(r3, r9)
                                        if (r3 == 0) goto L8a
                                        r14.add(r1)
                                    L8a:
                                        r1 = r24
                                        r3 = r20
                                        r9 = r21
                                        goto L67
                                    L91:
                                        r20 = r3
                                        r21 = r9
                                        r1 = r14
                                        java.util.List r1 = (java.util.List) r1
                                        java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r23)
                                        r2.L$0 = r3
                                        java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7)
                                        r2.L$1 = r3
                                        java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r6)
                                        r2.L$2 = r3
                                        java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r5)
                                        r2.L$3 = r3
                                        r2.I$0 = r8
                                        r3 = 1
                                        r2.label = r3
                                        java.lang.Object r1 = r5.emit(r1, r2)
                                        if (r1 != r4) goto Lbe
                                        return r4
                                    Lbe:
                                        r4 = r8
                                        r8 = r23
                                    Lc1:
                                        kotlin.Unit r1 = kotlin.Unit.INSTANCE
                                        return r1
                                    */
                                    throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$1.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                                }
                            }
                        };
                    }
                    final SchoolViewModel schoolViewModel = this.this$0;
                    final Flow $this$map$iv3 = $this$map$iv;
                    Flow baseFlow = new Flow<List<? extends Expense>>() { // from class: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2
                        public Object collect(FlowCollector collector, Continuation $completion) {
                            Object collect = $this$map$iv3.collect(new AnonymousClass2(collector, schoolViewModel, schoolYear), $completion);
                            return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
                        }

                        /* compiled from: Emitters.kt */
                        @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                        @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$expenses$1\n+ 4 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,49:1\n50#2:50\n248#3:51\n774#4:52\n865#4,2:53\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$expenses$1\n*L\n248#1:52\n248#1:53,2\n*E\n"})
                        /* renamed from: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2  reason: invalid class name */
                        /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2.dex */
                        public static final class AnonymousClass2<T> implements FlowCollector {
                            final /* synthetic */ String $schoolYear$inlined;
                            final /* synthetic */ FlowCollector $this_unsafeFlow;
                            final /* synthetic */ SchoolViewModel this$0;

                            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                            @DebugMetadata(c = "com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                            /* renamed from: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2$1  reason: invalid class name */
                            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2$1.dex */
                            public static final class AnonymousClass1 extends ContinuationImpl {
                                int I$0;
                                Object L$0;
                                Object L$1;
                                Object L$2;
                                Object L$3;
                                int label;
                                /* synthetic */ Object result;

                                public AnonymousClass1(Continuation continuation) {
                                    super(continuation);
                                }

                                public final Object invokeSuspend(Object obj) {
                                    this.result = obj;
                                    this.label |= Integer.MIN_VALUE;
                                    return AnonymousClass2.this.emit(null, (Continuation) this);
                                }
                            }

                            public AnonymousClass2(FlowCollector flowCollector, SchoolViewModel schoolViewModel, String str) {
                                this.$this_unsafeFlow = flowCollector;
                                this.this$0 = schoolViewModel;
                                this.$schoolYear$inlined = str;
                            }

                            /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                            /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                            /* JADX WARN: Removed duplicated region for block: B:13:0x0045  */
                            /*
                                Code decompiled incorrectly, please refer to instructions dump.
                                To view partially-correct add '--show-bad-code' argument
                            */
                            public final java.lang.Object emit(java.lang.Object r25, kotlin.coroutines.Continuation r26) {
                                /*
                                    r24 = this;
                                    r0 = r24
                                    r1 = r26
                                    boolean r2 = r1 instanceof com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2.AnonymousClass2.AnonymousClass1
                                    if (r2 == 0) goto L18
                                    r2 = r1
                                    com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2$1 r2 = (com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2.AnonymousClass2.AnonymousClass1) r2
                                    int r3 = r2.label
                                    r4 = -2147483648(0xffffffff80000000, float:-0.0)
                                    r3 = r3 & r4
                                    if (r3 == 0) goto L18
                                    int r3 = r2.label
                                    int r3 = r3 - r4
                                    r2.label = r3
                                    goto L1d
                                L18:
                                    com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2$1 r2 = new com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2$1
                                    r2.<init>(r1)
                                L1d:
                                    java.lang.Object r3 = r2.result
                                    java.lang.Object r4 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                                    int r5 = r2.label
                                    switch(r5) {
                                        case 0: goto L45;
                                        case 1: goto L30;
                                        default: goto L28;
                                    }
                                L28:
                                    java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
                                    java.lang.String r2 = "call to 'resume' before 'invoke' with coroutine"
                                    r1.<init>(r2)
                                    throw r1
                                L30:
                                    int r4 = r2.I$0
                                    java.lang.Object r5 = r2.L$3
                                    kotlinx.coroutines.flow.FlowCollector r5 = (kotlinx.coroutines.flow.FlowCollector) r5
                                    java.lang.Object r6 = r2.L$2
                                    java.lang.Object r7 = r2.L$1
                                    com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2$2$1 r7 = (com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2.AnonymousClass2.AnonymousClass1) r7
                                    java.lang.Object r8 = r2.L$0
                                    kotlin.ResultKt.throwOnFailure(r3)
                                    r20 = r3
                                    goto Lcf
                                L45:
                                    kotlin.ResultKt.throwOnFailure(r3)
                                    kotlinx.coroutines.flow.FlowCollector r5 = r0.$this_unsafeFlow
                                    r7 = r2
                                    r6 = r25
                                    r8 = 0
                                    r9 = r2
                                    kotlin.coroutines.Continuation r9 = (kotlin.coroutines.Continuation) r9
                                    r10 = r6
                                    java.util.List r10 = (java.util.List) r10
                                    r11 = 0
                                    r12 = r10
                                    java.lang.Iterable r12 = (java.lang.Iterable) r12
                                    r13 = 0
                                    java.util.ArrayList r14 = new java.util.ArrayList
                                    r14.<init>()
                                    java.util.Collection r14 = (java.util.Collection) r14
                                    r15 = r12
                                    r16 = 0
                                    java.util.Iterator r17 = r15.iterator()
                                L67:
                                    boolean r18 = r17.hasNext()
                                    if (r18 == 0) goto L9b
                                    java.lang.Object r1 = r17.next()
                                    r18 = r1
                                    com.example.data.models.Expense r18 = (com.example.data.models.Expense) r18
                                    r19 = 0
                                    r20 = r3
                                    com.example.ui.SchoolViewModel r3 = r0.this$0
                                    r21 = r9
                                    r22 = r10
                                    long r9 = r18.getDate()
                                    r23 = r11
                                    java.lang.String r11 = r0.$schoolYear$inlined
                                    boolean r3 = com.example.ui.SchoolViewModel.access$isTimestampInSchoolYear(r3, r9, r11)
                                    if (r3 == 0) goto L90
                                    r14.add(r1)
                                L90:
                                    r1 = r26
                                    r3 = r20
                                    r9 = r21
                                    r10 = r22
                                    r11 = r23
                                    goto L67
                                L9b:
                                    r20 = r3
                                    r21 = r9
                                    r22 = r10
                                    r23 = r11
                                    r1 = r14
                                    java.util.List r1 = (java.util.List) r1
                                    java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r25)
                                    r2.L$0 = r3
                                    java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7)
                                    r2.L$1 = r3
                                    java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r6)
                                    r2.L$2 = r3
                                    java.lang.Object r3 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r5)
                                    r2.L$3 = r3
                                    r2.I$0 = r8
                                    r3 = 1
                                    r2.label = r3
                                    java.lang.Object r1 = r5.emit(r1, r2)
                                    if (r1 != r4) goto Lcc
                                    return r4
                                Lcc:
                                    r4 = r8
                                    r8 = r25
                                Lcf:
                                    kotlin.Unit r1 = kotlin.Unit.INSTANCE
                                    return r1
                                */
                                throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$expenses$1$invokeSuspend$$inlined$map$2.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                            }
                        }
                    };
                    return baseFlow;
                }
                Flow baseFlow2 = FlowKt.flowOf(CollectionsKt.emptyList());
                return baseFlow2;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
