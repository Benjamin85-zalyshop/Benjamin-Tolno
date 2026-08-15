package com.example.ui;

import com.example.data.models.Student;
import com.example.data.repository.SchoolRepository;
import java.util.List;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Triple;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
/* compiled from: Merge.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\b\u0012\u0004\u0012\u0002H\u00020\u00042\u0006\u0010\u0005\u001a\u0002H\u0003H\n¨\u0006\u0006"}, d2 = {"<anonymous>", "", "R", "T", "Lkotlinx/coroutines/flow/FlowCollector;", "it", "kotlinx/coroutines/flow/FlowKt__MergeKt$flatMapLatest$1"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$special$$inlined$flatMapLatest$1", f = "SchoolViewModel.kt", i = {0, 0}, l = {189}, m = "invokeSuspend", n = {"$this$transformLatest", "it"}, s = {"L$0", "L$1"})
@SourceDebugExtension({"SMAP\nMerge.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Merge.kt\nkotlinx/coroutines/flow/FlowKt__MergeKt$flatMapLatest$1\n+ 2 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n+ 3 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 4 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt\n+ 5 SafeCollector.common.kt\nkotlinx/coroutines/flow/internal/SafeCollector_commonKt\n*L\n1#1,189:1\n219#2,2:190\n221#2:197\n49#3:192\n51#3:196\n49#3:198\n51#3:202\n46#4:193\n51#4:195\n46#4:199\n51#4:201\n105#5:194\n105#5:200\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n220#1:192\n220#1:196\n221#1:198\n221#1:202\n220#1:193\n220#1:195\n221#1:199\n221#1:201\n220#1:194\n221#1:200\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$flatMapLatest$1.dex */
public final class SchoolViewModel$special$$inlined$flatMapLatest$1 extends SuspendLambda implements Function3<FlowCollector<? super List<? extends Student>>, Triple<? extends Integer, ? extends String, ? extends String>, Continuation<? super Unit>, Object> {
    private /* synthetic */ Object L$0;
    /* synthetic */ Object L$1;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$special$$inlined$flatMapLatest$1(Continuation continuation, SchoolViewModel schoolViewModel) {
        super(3, continuation);
        this.this$0 = schoolViewModel;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj, Object obj2, Object obj3) {
        return invoke((FlowCollector) obj, (Triple<? extends Integer, ? extends String, ? extends String>) obj2, (Continuation) obj3);
    }

    public final Object invoke(FlowCollector<? super List<? extends Student>> flowCollector, Triple<? extends Integer, ? extends String, ? extends String> triple, Continuation<? super Unit> continuation) {
        SchoolViewModel$special$$inlined$flatMapLatest$1 schoolViewModel$special$$inlined$flatMapLatest$1 = new SchoolViewModel$special$$inlined$flatMapLatest$1(continuation, this.this$0);
        schoolViewModel$special$$inlined$flatMapLatest$1.L$0 = flowCollector;
        schoolViewModel$special$$inlined$flatMapLatest$1.L$1 = triple;
        return schoolViewModel$special$$inlined$flatMapLatest$1.invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        SchoolRepository schoolRepository;
        Flow $this$map$iv;
        SchoolRepository schoolRepository2;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                FlowCollector $this$transformLatest = (FlowCollector) this.L$0;
                Object it = this.L$1;
                Continuation continuation = (Continuation) this;
                Triple triple = (Triple) it;
                Integer id = (Integer) triple.component1();
                final String section = (String) triple.component2();
                final String year = (String) triple.component3();
                if (id == null) {
                    $this$map$iv = FlowKt.flowOf(CollectionsKt.emptyList());
                } else if (Intrinsics.areEqual(section, "Toutes les sections")) {
                    schoolRepository2 = this.this$0.repository;
                    final Flow $this$map$iv2 = schoolRepository2.getAllStudents(id.intValue());
                    $this$map$iv = new Flow<List<? extends Student>>() { // from class: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$1
                        public Object collect(FlowCollector collector, Continuation $completion) {
                            Object collect = $this$map$iv2.collect(new AnonymousClass2(collector, year), $completion);
                            return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
                        }

                        /* compiled from: Emitters.kt */
                        @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                        @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n+ 4 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 5 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,49:1\n50#2:50\n220#3:51\n774#4:52\n865#4:53\n866#4:55\n1#5:54\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n220#1:52\n220#1:53\n220#1:55\n*E\n"})
                        /* renamed from: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$1$2  reason: invalid class name */
                        /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$students$lambda$8$$inlined$map$1$2.dex */
                        public static final class AnonymousClass2<T> implements FlowCollector {
                            final /* synthetic */ FlowCollector $this_unsafeFlow;
                            final /* synthetic */ String $year$inlined;

                            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                            @DebugMetadata(c = "com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$1$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                            /* renamed from: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$1$2$1  reason: invalid class name */
                            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$students$lambda$8$$inlined$map$1$2$1.dex */
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
                                this.$year$inlined = str;
                            }

                            /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                            /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                            /* JADX WARN: Removed duplicated region for block: B:13:0x0045  */
                            /*
                                Code decompiled incorrectly, please refer to instructions dump.
                                To view partially-correct add '--show-bad-code' argument
                            */
                            public final java.lang.Object emit(java.lang.Object r24, kotlin.coroutines.Continuation r25) {
                                /*
                                    Method dump skipped, instructions count: 226
                                    To view this dump add '--comments-level debug' option
                                */
                                throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$1.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                            }
                        }
                    };
                } else {
                    schoolRepository = this.this$0.repository;
                    final Flow $this$map$iv3 = schoolRepository.getAllStudents(id.intValue());
                    $this$map$iv = new Flow<List<? extends Student>>() { // from class: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$2
                        public Object collect(FlowCollector collector, Continuation $completion) {
                            Object collect = $this$map$iv3.collect(new AnonymousClass2(collector, section, year), $completion);
                            return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
                        }

                        /* compiled from: Emitters.kt */
                        @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                        @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n+ 4 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 5 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,49:1\n50#2:50\n221#3:51\n774#4:52\n865#4:53\n866#4:55\n1#5:54\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n221#1:52\n221#1:53\n221#1:55\n*E\n"})
                        /* renamed from: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$2$2  reason: invalid class name */
                        /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$students$lambda$8$$inlined$map$2$2.dex */
                        public static final class AnonymousClass2<T> implements FlowCollector {
                            final /* synthetic */ String $section$inlined;
                            final /* synthetic */ FlowCollector $this_unsafeFlow;
                            final /* synthetic */ String $year$inlined;

                            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                            @DebugMetadata(c = "com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$2$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                            /* renamed from: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$2$2$1  reason: invalid class name */
                            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$students$lambda$8$$inlined$map$2$2$1.dex */
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

                            public AnonymousClass2(FlowCollector flowCollector, String str, String str2) {
                                this.$this_unsafeFlow = flowCollector;
                                this.$section$inlined = str;
                                this.$year$inlined = str2;
                            }

                            /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                            /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                            /* JADX WARN: Removed duplicated region for block: B:13:0x0045  */
                            /* JADX WARN: Removed duplicated region for block: B:29:0x00ad  */
                            /* JADX WARN: Removed duplicated region for block: B:39:0x00b0 A[SYNTHETIC] */
                            /*
                                Code decompiled incorrectly, please refer to instructions dump.
                                To view partially-correct add '--show-bad-code' argument
                            */
                            public final java.lang.Object emit(java.lang.Object r25, kotlin.coroutines.Continuation r26) {
                                /*
                                    Method dump skipped, instructions count: 242
                                    To view this dump add '--comments-level debug' option
                                */
                                throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$students$lambda$8$$inlined$map$2.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                            }
                        }
                    };
                }
                this.L$0 = SpillingKt.nullOutSpilledVariable($this$transformLatest);
                this.L$1 = SpillingKt.nullOutSpilledVariable(it);
                this.label = 1;
                if (FlowKt.emitAll($this$transformLatest, $this$map$iv, (Continuation) this) != coroutine_suspended) {
                    break;
                } else {
                    return coroutine_suspended;
                }
            case 1:
                Object obj = this.L$1;
                FlowCollector flowCollector = (FlowCollector) this.L$0;
                ResultKt.throwOnFailure($result);
                break;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        return Unit.INSTANCE;
    }
}
