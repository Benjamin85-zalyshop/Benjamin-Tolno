package com.example.ui;

import com.example.data.models.Subject;
import com.example.data.repository.SchoolRepository;
import com.google.firebase.firestore.FirebaseFirestore;
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
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$deleteSubject$1", f = "SchoolViewModel.kt", i = {}, l = {1231}, m = "invokeSuspend", n = {}, s = {})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$deleteSubject$1.dex */
final class SchoolViewModel$deleteSubject$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ Subject $subject;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$deleteSubject$1(Subject subject, String str, SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$deleteSubject$1> continuation) {
        super(2, continuation);
        this.$subject = subject;
        this.$email = str;
        this.this$0 = schoolViewModel;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$deleteSubject$1(this.$subject, this.$email, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    public final Object invokeSuspend(Object $result) {
        SchoolRepository schoolRepository;
        Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (this.label) {
            case 0:
                ResultKt.throwOnFailure($result);
                if (this.$subject.getRemoteId().length() > 0) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
                    try {
                        Intrinsics.checkNotNull(db.collection("schools").document(this.$email).collection("subjects").document(this.$subject.getRemoteId()).delete());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Unit unit = Unit.INSTANCE;
                    }
                }
                schoolRepository = this.this$0.repository;
                this.label = 1;
                if (schoolRepository.deleteSubjectById(this.$subject.getId(), (Continuation) this) == coroutine_suspended) {
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
