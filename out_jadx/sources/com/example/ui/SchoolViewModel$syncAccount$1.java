package com.example.ui;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel", f = "SchoolViewModel.kt", i = {0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8}, l = {1351, 1355, 1390, 1391, 1399, 1400, 1411, 1415, 1417}, m = "syncAccount", n = {"email", "account", "email", "account", "db", "email", "account", "db", "doc", "schoolName", "passwordHash", "financierPasswordHash", "displayName", "paymentPhoneNumber", "transactionId", "rejectionReason", "logoBase64", "address", "founderPhone", "newAcc", "hasActiveSubscription", "subscriptionExpiryDate", "isPendingValidation", "createdAt", "email", "account", "db", "doc", "schoolName", "passwordHash", "financierPasswordHash", "displayName", "paymentPhoneNumber", "transactionId", "rejectionReason", "logoBase64", "address", "founderPhone", "newAcc", "hasActiveSubscription", "subscriptionExpiryDate", "isPendingValidation", "createdAt", "email", "account", "email", "account", "email", "account", "auth", "resolvedRole", "targetEmail", "targetPassword", "currentEmail", "email", "account", "auth", "resolvedRole", "targetEmail", "targetPassword", "currentEmail", "e", "email", "account", "auth", "resolvedRole", "targetEmail", "targetPassword", "currentEmail", "e"}, s = {"L$0", "L$1", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "J$0", "I$1", "J$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "L$13", "L$14", "I$0", "J$0", "I$1", "J$1", "L$0", "L$1", "L$0", "L$1", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$syncAccount$1.dex */
public final class SchoolViewModel$syncAccount$1 extends ContinuationImpl {
    int I$0;
    int I$1;
    long J$0;
    long J$1;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$13;
    Object L$14;
    Object L$15;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    /* synthetic */ Object result;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$syncAccount$1(SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$syncAccount$1> continuation) {
        super(continuation);
        this.this$0 = schoolViewModel;
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object syncAccount;
        this.result = obj;
        this.label |= Integer.MIN_VALUE;
        syncAccount = this.this$0.syncAccount(null, (Continuation) this);
        return syncAccount;
    }
}
