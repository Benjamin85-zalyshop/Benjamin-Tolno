package com.example.ui;

import com.example.data.repository.SchoolRepository;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
/* compiled from: Merge.kt */
@Metadata(d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\b\u0012\u0004\u0012\u0002H\u00020\u00042\u0006\u0010\u0005\u001a\u0002H\u0003H\n¨\u0006\u0006"}, d2 = {"<anonymous>", "", "R", "T", "Lkotlinx/coroutines/flow/FlowCollector;", "it", "kotlinx/coroutines/flow/FlowKt__MergeKt$flatMapLatest$1"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$special$$inlined$flatMapLatest$5", f = "SchoolViewModel.kt", i = {0, 0}, l = {189}, m = "invokeSuspend", n = {"$this$transformLatest", "it"}, s = {"L$0", "L$1"})
@SourceDebugExtension({"SMAP\nMerge.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Merge.kt\nkotlinx/coroutines/flow/FlowKt__MergeKt$flatMapLatest$1\n+ 2 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n1#1,189:1\n262#2:190\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$flatMapLatest$5.dex */
public final class SchoolViewModel$special$$inlined$flatMapLatest$5 extends SuspendLambda implements Function3<FlowCollector<? super Boolean>, Integer, Continuation<? super Unit>, Object> {
    private /* synthetic */ Object L$0;
    /* synthetic */ Object L$1;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$special$$inlined$flatMapLatest$5(Continuation continuation, SchoolViewModel schoolViewModel) {
        super(3, continuation);
        this.this$0 = schoolViewModel;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj, Object obj2, Object obj3) {
        return invoke((FlowCollector) obj, (Integer) obj2, (Continuation) obj3);
    }

    public final Object invoke(FlowCollector<? super Boolean> flowCollector, Integer num, Continuation<? super Unit> continuation) {
        SchoolViewModel$special$$inlined$flatMapLatest$5 schoolViewModel$special$$inlined$flatMapLatest$5 = new SchoolViewModel$special$$inlined$flatMapLatest$5(continuation, this.this$0);
        schoolViewModel$special$$inlined$flatMapLatest$5.L$0 = flowCollector;
        schoolViewModel$special$$inlined$flatMapLatest$5.L$1 = num;
        return schoolViewModel$special$$inlined$flatMapLatest$5.invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        Flow flowOf;
        SchoolRepository schoolRepository;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                FlowCollector $this$transformLatest = (FlowCollector) this.L$0;
                Object it = this.L$1;
                Continuation continuation = (Continuation) this;
                Integer id = (Integer) it;
                if (id != null) {
                    schoolRepository = this.this$0.repository;
                    flowOf = schoolRepository.getPendingValidationStatus(id.intValue());
                } else {
                    flowOf = FlowKt.flowOf(Boxing.boxBoolean(false));
                }
                this.L$0 = SpillingKt.nullOutSpilledVariable($this$transformLatest);
                this.L$1 = SpillingKt.nullOutSpilledVariable(it);
                this.label = 1;
                if (FlowKt.emitAll($this$transformLatest, flowOf, (Continuation) this) != coroutine_suspended) {
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
