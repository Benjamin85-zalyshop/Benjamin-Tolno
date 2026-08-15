package com.example.ui;

import com.example.data.models.SchoolAccount;
import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function3;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u0010\u0000\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u0001H\n"}, d2 = {"<anonymous>", "", "account", "Lcom/example/data/models/SchoolAccount;", "trialActive"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$isAppAccessGranted$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$isAppAccessGranted$1.dex */
public final class SchoolViewModel$isAppAccessGranted$1 extends SuspendLambda implements Function3<SchoolAccount, Boolean, Continuation<? super Boolean>, Object> {
    /* synthetic */ Object L$0;
    /* synthetic */ boolean Z$0;
    int label;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SchoolViewModel$isAppAccessGranted$1(Continuation<? super SchoolViewModel$isAppAccessGranted$1> continuation) {
        super(3, continuation);
    }

    public final Object invoke(SchoolAccount schoolAccount, boolean z, Continuation<? super Boolean> continuation) {
        SchoolViewModel$isAppAccessGranted$1 schoolViewModel$isAppAccessGranted$1 = new SchoolViewModel$isAppAccessGranted$1(continuation);
        schoolViewModel$isAppAccessGranted$1.L$0 = schoolAccount;
        schoolViewModel$isAppAccessGranted$1.Z$0 = z;
        return schoolViewModel$isAppAccessGranted$1.invokeSuspend(Unit.INSTANCE);
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj, Object obj2, Object obj3) {
        return invoke((SchoolAccount) obj, ((Boolean) obj2).booleanValue(), (Continuation) obj3);
    }

    public final Object invokeSuspend(Object $result) {
        SchoolAccount account = (SchoolAccount) this.L$0;
        boolean trialActive = this.Z$0;
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                boolean z = false;
                if (account != null && ((account.getHasActiveSubscription() && (account.getSubscriptionExpiryDate() > System.currentTimeMillis() || account.getSubscriptionExpiryDate() == 0)) || trialActive)) {
                    z = true;
                }
                return Boxing.boxBoolean(z);
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
