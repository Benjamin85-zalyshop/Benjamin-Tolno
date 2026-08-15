package com.example.ui;

import com.google.firebase.auth.FirebaseUser;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.flow.MutableStateFlow;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$restoreSession$2", f = "SchoolViewModel.kt", i = {}, l = {206}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$restoreSession$2.dex */
public final class SchoolViewModel$restoreSession$2 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ FirebaseUser $currentUser;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$restoreSession$2(SchoolViewModel schoolViewModel, FirebaseUser firebaseUser, Continuation<? super SchoolViewModel$restoreSession$2> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$currentUser = firebaseUser;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$restoreSession$2(this.this$0, this.$currentUser, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        MutableStateFlow mutableStateFlow;
        Object syncAccount;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                mutableStateFlow = this.this$0._userRole;
                mutableStateFlow.setValue("FOUNDER");
                SchoolViewModel schoolViewModel = this.this$0;
                String email = this.$currentUser.getEmail();
                Intrinsics.checkNotNull(email);
                this.label = 1;
                syncAccount = schoolViewModel.syncAccount(email, (Continuation) this);
                if (syncAccount == coroutine_suspended) {
                    return coroutine_suspended;
                }
                break;
            case 1:
                ResultKt.throwOnFailure($result);
                break;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        return Unit.INSTANCE;
    }
}
