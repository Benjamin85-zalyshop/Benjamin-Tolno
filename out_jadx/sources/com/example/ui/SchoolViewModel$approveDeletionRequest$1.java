package com.example.ui;

import com.example.data.models.DeletionRequest;
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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$approveDeletionRequest$1", f = "SchoolViewModel.kt", i = {0, 0, 1, 1, 1, 1}, l = {480, 483}, m = "invokeSuspend", n = {"db", "schoolRef", "db", "schoolRef", "studentId", "schoolId"}, s = {"L$0", "L$1", "L$0", "L$1", "L$2", "I$0"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$approveDeletionRequest$1\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,2219:1\n774#2:2220\n865#2,2:2221\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$approveDeletionRequest$1\n*L\n484#1:2220\n484#1:2221,2\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$approveDeletionRequest$1.dex */
final class SchoolViewModel$approveDeletionRequest$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ DeletionRequest $request;
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$approveDeletionRequest$1(String str, DeletionRequest deletionRequest, SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$approveDeletionRequest$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$request = deletionRequest;
        this.this$0 = schoolViewModel;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$approveDeletionRequest$1(this.$email, this.$request, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x00de A[Catch: Exception -> 0x0150, TryCatch #0 {Exception -> 0x0150, blocks: (B:7:0x0022, B:26:0x00c4, B:27:0x00d8, B:29:0x00de, B:37:0x00f9, B:32:0x00ee, B:39:0x00fe, B:40:0x0108, B:42:0x010e, B:47:0x0125, B:48:0x0137, B:10:0x0033, B:16:0x008c, B:18:0x0090, B:20:0x009e, B:22:0x00a4, B:13:0x0059), top: B:54:0x0009 }] */
    /* JADX WARN: Removed duplicated region for block: B:42:0x010e A[Catch: Exception -> 0x0150, TryCatch #0 {Exception -> 0x0150, blocks: (B:7:0x0022, B:26:0x00c4, B:27:0x00d8, B:29:0x00de, B:37:0x00f9, B:32:0x00ee, B:39:0x00fe, B:40:0x0108, B:42:0x010e, B:47:0x0125, B:48:0x0137, B:10:0x0033, B:16:0x008c, B:18:0x0090, B:20:0x009e, B:22:0x00a4, B:13:0x0059), top: B:54:0x0009 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r18) {
        /*
            Method dump skipped, instructions count: 354
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$approveDeletionRequest$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
