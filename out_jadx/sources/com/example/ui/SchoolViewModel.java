package com.example.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.compose.runtime.internal.StabilityInferred;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import com.example.data.models.ClassFee;
import com.example.data.models.DeletionRequest;
import com.example.data.models.Expense;
import com.example.data.models.Payment;
import com.example.data.models.SchoolAccount;
import com.example.data.models.Student;
import com.example.data.models.StudentGrade;
import com.example.data.models.Subject;
import com.example.data.repository.SchoolRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Triple;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: SchoolViewModel.kt */
@StabilityInferred(parameters = 0)
@Metadata(d1 = {"\u0000¸\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b(\n\u0002\u0010\u0007\n\u0002\b'\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J\u000e\u00102\u001a\u0002032\u0006\u00104\u001a\u00020\u0010J\u0018\u00105\u001a\u0002062\u0006\u00107\u001a\u0002082\u0006\u00109\u001a\u00020\u0010H\u0002J\u0018\u0010:\u001a\u0002082\u0006\u00107\u001a\u0002082\u0006\u00109\u001a\u00020\u0010H\u0002J\u0018\u0010?\u001a\u0002032\u0006\u0010@\u001a\u00020\u00102\u0006\u0010A\u001a\u00020\u0010H\u0002J\b\u0010B\u001a\u000203H\u0002J\b\u0010C\u001a\u000203H\u0002J\u000e\u0010D\u001a\u0002032\u0006\u0010E\u001a\u00020\u0010JR\u0010b\u001a\u0002032\u0006\u0010c\u001a\u00020\u00102\u0006\u0010d\u001a\u00020\u00102\u0006\u0010e\u001a\u00020\u00102\u0006\u0010E\u001a\u00020\u00102\n\b\u0002\u0010f\u001a\u0004\u0018\u00010\u00102\b\b\u0002\u0010g\u001a\u0002082\b\b\u0002\u0010h\u001a\u0002082\n\b\u0002\u0010i\u001a\u0004\u0018\u00010\u0010J\u0018\u0010j\u001a\u0002032\u0006\u0010k\u001a\u00020G2\b\u0010i\u001a\u0004\u0018\u00010\u0010J\u0016\u0010l\u001a\u0002032\u0006\u0010e\u001a\u00020\u00102\u0006\u0010m\u001a\u000208J\u0010\u0010n\u001a\u0002032\b\u0010o\u001a\u0004\u0018\u00010\u0010J\u0016\u0010p\u001a\u0002032\u0006\u0010k\u001a\u00020G2\u0006\u0010q\u001a\u00020\u0010J\u000e\u0010r\u001a\u0002032\u0006\u0010s\u001a\u00020+J\u0016\u0010t\u001a\u0002032\u0006\u0010s\u001a\u00020+2\u0006\u0010q\u001a\u00020\u0010J\u000e\u0010u\u001a\u0002032\u0006\u0010s\u001a\u00020+J\u000e\u0010v\u001a\u0002032\u0006\u0010k\u001a\u00020GJ(\u0010w\u001a\u0002032\u0006\u0010x\u001a\u00020\n2\u0006\u0010m\u001a\u0002082\u0006\u0010q\u001a\u00020\u00102\b\b\u0002\u0010y\u001a\u00020\u0010J\u000e\u0010z\u001a\u0002032\u0006\u0010{\u001a\u00020\nJ&\u0010|\u001a\u0002032\u0006\u0010m\u001a\u0002082\u0006\u0010}\u001a\u00020\u00102\u0006\u0010~\u001a\u00020\u00102\u0006\u0010E\u001a\u00020\u0010J\u000f\u0010\u007f\u001a\u0002032\u0007\u0010\u0080\u0001\u001a\u00020\nJ\u0010\u0010\u0081\u0001\u001a\u000206H\u0086@¢\u0006\u0003\u0010\u0082\u0001J\u001a\u0010\u0083\u0001\u001a\u0002032\u0006\u0010@\u001a\u00020\u00102\u0007\u0010\u0084\u0001\u001a\u00020\nH\u0002J5\u0010\u0085\u0001\u001a\u0002032\u0006\u0010E\u001a\u00020\u00102\u0006\u0010e\u001a\u00020\u00102\u0007\u0010\u0086\u0001\u001a\u00020\u00102\u0007\u0010\u0087\u0001\u001a\u00020\n2\n\b\u0002\u0010\u0088\u0001\u001a\u00030\u0089\u0001J\u0010\u0010\u008a\u0001\u001a\u0002032\u0007\u0010\u008b\u0001\u001a\u00020]J\u0017\u0010\u008c\u0001\u001a\u0002032\u0006\u0010E\u001a\u00020\u00102\u0006\u0010e\u001a\u00020\u0010J^\u0010\u008d\u0001\u001a\u0002032\u0006\u0010x\u001a\u00020\n2\u0007\u0010\u008e\u0001\u001a\u00020\u00102\u0007\u0010\u008f\u0001\u001a\u00020\n2\u0007\u0010\u0090\u0001\u001a\u00020\u00102\u0007\u0010\u0091\u0001\u001a\u00020\u00102\n\u0010\u0092\u0001\u001a\u0005\u0018\u00010\u0089\u00012\n\u0010\u0093\u0001\u001a\u0005\u0018\u00010\u0089\u00012\u000b\b\u0002\u0010\u0094\u0001\u001a\u0004\u0018\u00010\u0010¢\u0006\u0003\u0010\u0095\u0001J\u0018\u0010\u0096\u0001\u001a\u0002032\u0006\u0010@\u001a\u00020\u0010H\u0082@¢\u0006\u0003\u0010\u0097\u0001JI\u0010\u0098\u0001\u001a\u0002062\u0006\u0010@\u001a\u00020\u00102\u0007\u0010\u0099\u0001\u001a\u00020\u00102\u0007\u0010\u009a\u0001\u001a\u00020\u00102\u0007\u0010\u009b\u0001\u001a\u00020\u00102\t\b\u0002\u0010\u009c\u0001\u001a\u00020\u00102\t\b\u0002\u0010\u009d\u0001\u001a\u00020\u0010H\u0086@¢\u0006\u0003\u0010\u009e\u0001J+\u0010\u009f\u0001\u001a\u0002032\u0007\u0010 \u0001\u001a\u00020\u00102\u0007\u0010\u0099\u0001\u001a\u00020\u00102\u0007\u0010\u009a\u0001\u001a\u00020\u0010H\u0082@¢\u0006\u0003\u0010¡\u0001J!\u0010¢\u0001\u001a\u0002062\u0006\u0010@\u001a\u00020\u00102\u0007\u0010£\u0001\u001a\u00020\u0010H\u0086@¢\u0006\u0003\u0010¤\u0001J\u001a\u0010¥\u0001\u001a\u0002032\u0007\u0010\u0084\u0001\u001a\u00020\n2\u0006\u0010x\u001a\u00020\nH\u0002J!\u0010¦\u0001\u001a\u0002032\u0007\u0010\u0084\u0001\u001a\u00020\n2\u0006\u0010x\u001a\u00020\n2\u0007\u0010\u0091\u0001\u001a\u00020\u0010J\u0007\u0010§\u0001\u001a\u000203J\u0007\u0010¨\u0001\u001a\u000203J\u0010\u0010¬\u0001\u001a\u0002032\u0007\u0010\u00ad\u0001\u001a\u00020\u0010J\t\u0010«\u0001\u001a\u0004\u0018\u00010\u0010J\u0007\u0010®\u0001\u001a\u000203J!\u0010¯\u0001\u001a\u0002032\u0018\b\u0002\u0010°\u0001\u001a\u0011\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u000203\u0018\u00010±\u0001J\u0019\u0010²\u0001\u001a\u0002032\u0007\u0010³\u0001\u001a\u00020\u00102\u0007\u0010´\u0001\u001a\u00020\u0010J\u0018\u0010µ\u0001\u001a\u0002062\u0006\u0010@\u001a\u00020\u0010H\u0086@¢\u0006\u0003\u0010\u0097\u0001J\u0010\u0010¶\u0001\u001a\u0002032\u0007\u0010·\u0001\u001a\u00020\u0010J\u0007\u0010¿\u0001\u001a\u000203J\u000f\u0010À\u0001\u001a\u0002032\u0006\u0010@\u001a\u00020\u0010J\u000f\u0010Á\u0001\u001a\u0002032\u0006\u0010@\u001a\u00020\u0010J\u0017\u0010Â\u0001\u001a\u0002032\u0006\u0010@\u001a\u00020\u00102\u0006\u0010q\u001a\u00020\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0019\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\f¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0019\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f¢\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00100\f¢\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u000eR\u0016\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0019\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f¢\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u000eR\u0016\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0019\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f¢\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u000eR\u0016\u0010\u001c\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001d0\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0019\u0010\u001e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001d0\f¢\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u000eR\u0016\u0010 \u001a\n \"*\u0004\u0018\u00010!0!X\u0082\u0004¢\u0006\u0002\n\u0000R\u0016\u0010#\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0019\u0010$\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f¢\u0006\b\n\u0000\u001a\u0004\b%\u0010\u000eR\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u0017\u0010'\u001a\b\u0012\u0004\u0012\u00020\u00100\f¢\u0006\b\n\u0000\u001a\u0004\b(\u0010\u000eR\u001a\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020+0*0\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u001d\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020+0*0\f¢\u0006\b\n\u0000\u001a\u0004\b-\u0010\u000eR\u001a\u0010.\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020/0*0\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u001d\u00100\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020/0*0\f¢\u0006\b\n\u0000\u001a\u0004\b1\u0010\u000eR\u0014\u0010;\u001a\b\u0012\u0004\u0012\u00020=0<X\u0082\u0004¢\u0006\u0002\n\u0000R\u0010\u0010>\u001a\u0004\u0018\u00010=X\u0082\u000e¢\u0006\u0002\n\u0000R\u001d\u0010F\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020G0*0\f¢\u0006\b\n\u0000\u001a\u0004\bH\u0010\u000eR\u001d\u0010I\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020J0*0\f¢\u0006\b\n\u0000\u001a\u0004\bK\u0010\u000eR\u0019\u0010L\u001a\n\u0012\u0006\u0012\u0004\u0018\u0001080\f¢\u0006\b\n\u0000\u001a\u0004\bM\u0010\u000eR\u001d\u0010N\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020O0*0\f¢\u0006\b\n\u0000\u001a\u0004\bP\u0010\u000eR\u0019\u0010Q\u001a\n\u0012\u0006\u0012\u0004\u0018\u0001080\f¢\u0006\b\n\u0000\u001a\u0004\bR\u0010\u000eR\u0017\u0010S\u001a\b\u0012\u0004\u0012\u0002060\f¢\u0006\b\n\u0000\u001a\u0004\bT\u0010\u000eR\u0017\u0010U\u001a\b\u0012\u0004\u0012\u0002060\f¢\u0006\b\n\u0000\u001a\u0004\bU\u0010\u000eR\u0017\u0010V\u001a\b\u0012\u0004\u0012\u0002060\f¢\u0006\b\n\u0000\u001a\u0004\bV\u0010\u000eR\u0017\u0010W\u001a\b\u0012\u0004\u0012\u0002080\f¢\u0006\b\n\u0000\u001a\u0004\bX\u0010\u000eR\u0017\u0010Y\u001a\b\u0012\u0004\u0012\u0002060\f¢\u0006\b\n\u0000\u001a\u0004\bY\u0010\u000eR\u0017\u0010Z\u001a\b\u0012\u0004\u0012\u0002080\f¢\u0006\b\n\u0000\u001a\u0004\b[\u0010\u000eR\u001d\u0010\\\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020]0*0\f¢\u0006\b\n\u0000\u001a\u0004\b^\u0010\u000eR\u001d\u0010_\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020`0*0\f¢\u0006\b\n\u0000\u001a\u0004\ba\u0010\u000eR\u0017\u0010©\u0001\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u001b\u0010ª\u0001\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f¢\u0006\t\n\u0000\u001a\u0005\b«\u0001\u0010\u000eR\u001c\u0010¸\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030¹\u00010*0\tX\u0082\u0004¢\u0006\u0002\n\u0000R \u0010º\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030¹\u00010*0\f¢\u0006\t\n\u0000\u001a\u0005\b»\u0001\u0010\u000eR\u0017\u0010¼\u0001\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\tX\u0082\u0004¢\u0006\u0002\n\u0000R\u001b\u0010½\u0001\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f¢\u0006\t\n\u0000\u001a\u0005\b¾\u0001\u0010\u000e¨\u0006Ã\u0001"}, d2 = {"Lcom/example/ui/SchoolViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/example/data/repository/SchoolRepository;", "context", "Landroid/content/Context;", "<init>", "(Lcom/example/data/repository/SchoolRepository;Landroid/content/Context;)V", "_currentSchoolId", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "currentSchoolId", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentSchoolId", "()Lkotlinx/coroutines/flow/StateFlow;", "_schoolName", "", "schoolName", "getSchoolName", "_selectedSection", "selectedSection", "getSelectedSection", "_userRole", "userRole", "getUserRole", "_loginError", "loginError", "getLoginError", "_schoolAccount", "Lcom/example/data/models/SchoolAccount;", "schoolAccount", "getSchoolAccount", "sharedPrefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "_schoolLogoBase64", "schoolLogoBase64", "getSchoolLogoBase64", "_selectedSchoolYear", "selectedSchoolYear", "getSelectedSchoolYear", "_deletionRequests", "", "Lcom/example/data/models/DeletionRequest;", "deletionRequests", "getDeletionRequests", "_classFees", "Lcom/example/data/models/ClassFee;", "classFees", "getClassFees", "setSelectedSchoolYear", "", "year", "isTimestampInSchoolYear", "", "timestamp", "", "schoolYear", "adjustTimestampToSchoolYear", "activeListeners", "", "Lcom/google/firebase/firestore/ListenerRegistration;", "adminListener", "saveSession", "email", "role", "clearSession", "restoreSession", "setSection", "section", "students", "Lcom/example/data/models/Student;", "getStudents", "payments", "Lcom/example/data/models/Payment;", "getPayments", "totalCollected", "getTotalCollected", "expenses", "Lcom/example/data/models/Expense;", "getExpenses", "totalExpenses", "getTotalExpenses", "hasActiveSubscription", "getHasActiveSubscription", "isPendingValidation", "isTrialActive", "trialDaysRemaining", "getTrialDaysRemaining", "isAppAccessGranted", "balance", "getBalance", "subjects", "Lcom/example/data/models/Subject;", "getSubjects", "grades", "Lcom/example/data/models/StudentGrade;", "getGrades", "insertStudent", "firstName", "lastName", "grade", "parentWhatsApp", "registrationFee", "reenrollmentFee", "photoBase64", "updateStudentPhoto", "student", "setClassFee", "amount", "setSchoolLogo", "base64", "createDeletionRequest", "reason", "approveDeletionRequest", "request", "rejectDeletionRequest", "dismissDeletionRequest", "deleteStudentDirectly", "insertPayment", "studentId", "paymentMethod", "deletePayment", "paymentId", "insertExpense", "category", "description", "deleteExpense", "expenseId", "hasAccount", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "startRealtimeSync", "schoolId", "insertSubject", "name", "coefficient", "maxScore", "", "deleteSubject", "subject", "seedDefaultSubjects", "saveGrade", "studentRemoteId", "subjectId", "subjectRemoteId", "term", "evaluationScore", "examScore", "comment", "(ILjava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/Float;Ljava/lang/Float;Ljava/lang/String;)V", "syncAccount", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "registerSchool", "founderPassword", "financierPassword", "displayName", "address", "founderPhone", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncFinancierAuthAccount", "schoolEmail", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "rawPassword", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateStudentFinancialsInRTDB", "syncStudentAcademicsToRTDB", "logout", "activateSubscription", "_pendingOrderId", "pendingOrderId", "getPendingOrderId", "savePendingOrderId", "orderId", "clearPendingOrderId", "checkPendingPaymentStatus", "onResult", "Lkotlin/Function1;", "submitSubscriptionRequest", "phoneNumber", "transactionId", "sendPasswordResetEmail", "updateFinancierPassword", "newPassword", "_adminSchools", "Lcom/example/ui/SchoolAdminItem;", "adminSchools", "getAdminSchools", "_adminError", "adminError", "getAdminError", "loadAdminSchools", "approveSchoolSubscription", "deleteSchoolAccount", "rejectSchoolSubscription", "app_debug"}, k = 1, mv = {2, 2, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n+ 2 Merge.kt\nkotlinx/coroutines/flow/FlowKt__MergeKt\n+ 3 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 4 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt\n+ 5 SafeCollector.common.kt\nkotlinx/coroutines/flow/internal/SafeCollector_commonKt\n+ 6 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 7 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n189#2:2220\n189#2:2221\n189#2:2227\n189#2:2233\n189#2:2234\n189#2:2245\n189#2:2246\n49#3:2222\n51#3:2226\n49#3:2228\n51#3:2232\n49#3:2235\n51#3:2239\n49#3:2240\n51#3:2244\n46#4:2223\n51#4:2225\n46#4:2229\n51#4:2231\n46#4:2236\n51#4:2238\n46#4:2241\n51#4:2243\n105#5:2224\n105#5:2230\n105#5:2237\n105#5:2242\n1563#6:2247\n1634#6,3:2248\n1563#6:2251\n1634#6,3:2252\n1869#6,2:2255\n1869#6,2:2257\n1869#6,2:2260\n1617#6,9:2262\n1869#6:2271\n1870#6:2273\n1626#6:2274\n1617#6,9:2275\n1869#6:2284\n1870#6:2286\n1626#6:2287\n1669#6,8:2288\n1617#6,9:2296\n1869#6:2305\n1870#6:2307\n1626#6:2308\n1068#6:2309\n1#7:2259\n1#7:2272\n1#7:2285\n1#7:2306\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n218#1:2220\n236#1:2221\n251#1:2227\n258#1:2233\n262#1:2234\n293#1:2245\n297#1:2246\n239#1:2222\n239#1:2226\n254#1:2228\n254#1:2232\n265#1:2235\n265#1:2239\n274#1:2240\n274#1:2244\n239#1:2223\n239#1:2225\n254#1:2229\n254#1:2231\n265#1:2236\n265#1:2238\n274#1:2241\n274#1:2243\n239#1:2224\n254#1:2230\n265#1:2237\n274#1:2242\n102#1:2247\n102#1:2248,3\n125#1:2251\n125#1:2252,3\n677#1:2255,2\n1295#1:2257,2\n1958#1:2260,2\n1002#1:2262,9\n1002#1:2271\n1002#1:2273\n1002#1:2274\n1037#1:2275,9\n1037#1:2284\n1037#1:2286\n1037#1:2287\n1058#1:2288,8\n2105#1:2296,9\n2105#1:2305\n2105#1:2307\n2105#1:2308\n2136#1:2309\n1002#1:2272\n1037#1:2285\n2105#1:2306\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel.dex */
public final class SchoolViewModel extends ViewModel {
    public static final int $stable = 8;
    @NotNull
    private final MutableStateFlow<String> _adminError;
    @NotNull
    private final MutableStateFlow<List<SchoolAdminItem>> _adminSchools;
    @NotNull
    private final MutableStateFlow<List<ClassFee>> _classFees;
    @NotNull
    private final MutableStateFlow<Integer> _currentSchoolId;
    @NotNull
    private final MutableStateFlow<List<DeletionRequest>> _deletionRequests;
    @NotNull
    private final MutableStateFlow<String> _loginError;
    @NotNull
    private final MutableStateFlow<String> _pendingOrderId;
    @NotNull
    private final MutableStateFlow<SchoolAccount> _schoolAccount;
    @NotNull
    private final MutableStateFlow<String> _schoolLogoBase64;
    @NotNull
    private final MutableStateFlow<String> _schoolName;
    @NotNull
    private final MutableStateFlow<String> _selectedSchoolYear;
    @NotNull
    private final MutableStateFlow<String> _selectedSection;
    @NotNull
    private final MutableStateFlow<String> _userRole;
    @NotNull
    private final List<ListenerRegistration> activeListeners;
    @NotNull
    private final StateFlow<String> adminError;
    @Nullable
    private ListenerRegistration adminListener;
    @NotNull
    private final StateFlow<List<SchoolAdminItem>> adminSchools;
    @NotNull
    private final StateFlow<Long> balance;
    @NotNull
    private final StateFlow<List<ClassFee>> classFees;
    @NotNull
    private final Context context;
    @NotNull
    private final StateFlow<Integer> currentSchoolId;
    @NotNull
    private final StateFlow<List<DeletionRequest>> deletionRequests;
    @NotNull
    private final StateFlow<List<Expense>> expenses;
    @NotNull
    private final StateFlow<List<StudentGrade>> grades;
    @NotNull
    private final StateFlow<Boolean> hasActiveSubscription;
    @NotNull
    private final StateFlow<Boolean> isAppAccessGranted;
    @NotNull
    private final StateFlow<Boolean> isPendingValidation;
    @NotNull
    private final StateFlow<Boolean> isTrialActive;
    @NotNull
    private final StateFlow<String> loginError;
    @NotNull
    private final StateFlow<List<Payment>> payments;
    @NotNull
    private final StateFlow<String> pendingOrderId;
    @NotNull
    private final SchoolRepository repository;
    @NotNull
    private final StateFlow<SchoolAccount> schoolAccount;
    @NotNull
    private final StateFlow<String> schoolLogoBase64;
    @NotNull
    private final StateFlow<String> schoolName;
    @NotNull
    private final StateFlow<String> selectedSchoolYear;
    @NotNull
    private final StateFlow<String> selectedSection;
    private final SharedPreferences sharedPrefs;
    @NotNull
    private final StateFlow<List<Student>> students;
    @NotNull
    private final StateFlow<List<Subject>> subjects;
    @NotNull
    private final StateFlow<Long> totalCollected;
    @NotNull
    private final StateFlow<Long> totalExpenses;
    @NotNull
    private final StateFlow<Long> trialDaysRemaining;
    @NotNull
    private final StateFlow<String> userRole;

    public SchoolViewModel(@NotNull SchoolRepository repository, @NotNull Context context) {
        Intrinsics.checkNotNullParameter(repository, "repository");
        Intrinsics.checkNotNullParameter(context, "context");
        this.repository = repository;
        this.context = context;
        this._currentSchoolId = StateFlowKt.MutableStateFlow((Object) null);
        this.currentSchoolId = this._currentSchoolId;
        this._schoolName = StateFlowKt.MutableStateFlow((Object) null);
        this.schoolName = this._schoolName;
        this._selectedSection = StateFlowKt.MutableStateFlow("Toutes les sections");
        this.selectedSection = this._selectedSection;
        this._userRole = StateFlowKt.MutableStateFlow("FOUNDER");
        this.userRole = this._userRole;
        this._loginError = StateFlowKt.MutableStateFlow((Object) null);
        this.loginError = this._loginError;
        this._schoolAccount = StateFlowKt.MutableStateFlow((Object) null);
        this.schoolAccount = this._schoolAccount;
        this.sharedPrefs = this.context.getSharedPreferences("scolapay_prefs", 0);
        this._schoolLogoBase64 = StateFlowKt.MutableStateFlow(this.sharedPrefs.getString("school_logo_base64", null));
        this.schoolLogoBase64 = this._schoolLogoBase64;
        String string = this.sharedPrefs.getString("selected_school_year", "2024 - 2025");
        this._selectedSchoolYear = StateFlowKt.MutableStateFlow(string != null ? string : "2024 - 2025");
        this.selectedSchoolYear = this._selectedSchoolYear;
        this._deletionRequests = StateFlowKt.MutableStateFlow(CollectionsKt.emptyList());
        this.deletionRequests = this._deletionRequests;
        this._classFees = StateFlowKt.MutableStateFlow(CollectionsKt.emptyList());
        this.classFees = this._classFees;
        this.activeListeners = new ArrayList();
        restoreSession();
        Flow $this$flatMapLatest$iv = FlowKt.combine(this._currentSchoolId, this._selectedSection, this._selectedSchoolYear, new SchoolViewModel$students$1(null));
        this.students = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv, new SchoolViewModel$special$$inlined$flatMapLatest$1(null, this)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), CollectionsKt.emptyList());
        Flow $this$flatMapLatest$iv2 = FlowKt.combine(this._currentSchoolId, this._selectedSection, this.students, this._selectedSchoolYear, new SchoolViewModel$payments$1(this, null));
        this.payments = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv2, new SchoolViewModel$special$$inlined$flatMapLatest$2(null)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), CollectionsKt.emptyList());
        final Flow $this$map$iv = this.payments;
        this.totalCollected = FlowKt.stateIn(new Flow<Long>() { // from class: com.example.ui.SchoolViewModel$special$$inlined$map$1
            public Object collect(FlowCollector collector, Continuation $completion) {
                Object collect = $this$map$iv.collect(new AnonymousClass2(collector), $completion);
                return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
            }

            /* compiled from: Emitters.kt */
            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
            @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,49:1\n50#2:50\n239#3:51\n1#4:52\n*E\n"})
            /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$1$2  reason: invalid class name */
            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$1$2.dex */
            public static final class AnonymousClass2<T> implements FlowCollector {
                final /* synthetic */ FlowCollector $this_unsafeFlow;

                @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                @DebugMetadata(c = "com.example.ui.SchoolViewModel$special$$inlined$map$1$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$1$2$1  reason: invalid class name */
                /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$1$2$1.dex */
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

                public AnonymousClass2(FlowCollector flowCollector) {
                    this.$this_unsafeFlow = flowCollector;
                }

                /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                /* JADX WARN: Removed duplicated region for block: B:13:0x0043  */
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                    To view partially-correct add '--show-bad-code' argument
                */
                public final java.lang.Object emit(java.lang.Object r20, kotlin.coroutines.Continuation r21) {
                    /*
                        r19 = this;
                        r0 = r19
                        r1 = r21
                        boolean r2 = r1 instanceof com.example.ui.SchoolViewModel$special$$inlined$map$1.AnonymousClass2.AnonymousClass1
                        if (r2 == 0) goto L18
                        r2 = r1
                        com.example.ui.SchoolViewModel$special$$inlined$map$1$2$1 r2 = (com.example.ui.SchoolViewModel$special$$inlined$map$1.AnonymousClass2.AnonymousClass1) r2
                        int r3 = r2.label
                        r4 = -2147483648(0xffffffff80000000, float:-0.0)
                        r3 = r3 & r4
                        if (r3 == 0) goto L18
                        int r3 = r2.label
                        int r3 = r3 - r4
                        r2.label = r3
                        goto L1d
                    L18:
                        com.example.ui.SchoolViewModel$special$$inlined$map$1$2$1 r2 = new com.example.ui.SchoolViewModel$special$$inlined$map$1$2$1
                        r2.<init>(r1)
                    L1d:
                        java.lang.Object r3 = r2.result
                        java.lang.Object r4 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                        int r5 = r2.label
                        switch(r5) {
                            case 0: goto L43;
                            case 1: goto L30;
                            default: goto L28;
                        }
                    L28:
                        java.lang.IllegalStateException r2 = new java.lang.IllegalStateException
                        java.lang.String r3 = "call to 'resume' before 'invoke' with coroutine"
                        r2.<init>(r3)
                        throw r2
                    L30:
                        int r4 = r2.I$0
                        java.lang.Object r5 = r2.L$3
                        kotlinx.coroutines.flow.FlowCollector r5 = (kotlinx.coroutines.flow.FlowCollector) r5
                        java.lang.Object r6 = r2.L$2
                        java.lang.Object r7 = r2.L$1
                        com.example.ui.SchoolViewModel$special$$inlined$map$1$2$1 r7 = (com.example.ui.SchoolViewModel$special$$inlined$map$1.AnonymousClass2.AnonymousClass1) r7
                        java.lang.Object r8 = r2.L$0
                        kotlin.ResultKt.throwOnFailure(r3)
                        goto Lbf
                    L43:
                        kotlin.ResultKt.throwOnFailure(r3)
                        kotlinx.coroutines.flow.FlowCollector r5 = r0.$this_unsafeFlow
                        r7 = r2
                        r6 = r20
                        r8 = 0
                        r9 = r2
                        kotlin.coroutines.Continuation r9 = (kotlin.coroutines.Continuation) r9
                        r10 = r6
                        java.util.List r10 = (java.util.List) r10
                        r11 = 0
                        r12 = r10
                        java.lang.Iterable r12 = (java.lang.Iterable) r12
                        java.util.Iterator r12 = r12.iterator()
                        r13 = 0
                        r15 = r13
                    L5d:
                        boolean r17 = r12.hasNext()
                        if (r17 == 0) goto L73
                        java.lang.Object r17 = r12.next()
                        com.example.data.models.Payment r17 = (com.example.data.models.Payment) r17
                        r18 = 0
                        long r17 = r17.getAmount()
                        long r15 = r15 + r17
                        goto L5d
                    L73:
                        java.lang.Long r12 = kotlin.coroutines.jvm.internal.Boxing.boxLong(r15)
                        r15 = r12
                        java.lang.Number r15 = (java.lang.Number) r15
                        long r15 = r15.longValue()
                        r17 = 0
                        int r18 = (r15 > r13 ? 1 : (r15 == r13 ? 0 : -1))
                        r13 = 1
                        if (r18 <= 0) goto L87
                        r14 = r13
                        goto L88
                    L87:
                        r14 = 0
                    L88:
                        if (r14 == 0) goto L8b
                        goto L8c
                    L8b:
                        r12 = 0
                    L8c:
                        if (r12 == 0) goto L93
                        long r14 = r12.longValue()
                        goto L95
                    L93:
                        r14 = 0
                    L95:
                        java.lang.Long r9 = kotlin.coroutines.jvm.internal.Boxing.boxLong(r14)
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r20)
                        r2.L$0 = r10
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7)
                        r2.L$1 = r10
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r6)
                        r2.L$2 = r10
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r5)
                        r2.L$3 = r10
                        r2.I$0 = r8
                        r2.label = r13
                        java.lang.Object r9 = r5.emit(r9, r2)
                        if (r9 != r4) goto Lbc
                        return r4
                    Lbc:
                        r4 = r8
                        r8 = r20
                    Lbf:
                        kotlin.Unit r4 = kotlin.Unit.INSTANCE
                        return r4
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$special$$inlined$map$1.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                }
            }
        }, ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), 0L);
        Flow $this$flatMapLatest$iv3 = FlowKt.combine(this._currentSchoolId, this._selectedSection, this._selectedSchoolYear, new SchoolViewModel$expenses$1(this, null));
        this.expenses = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv3, new SchoolViewModel$special$$inlined$flatMapLatest$3(null)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), CollectionsKt.emptyList());
        final Flow $this$map$iv2 = this.expenses;
        this.totalExpenses = FlowKt.stateIn(new Flow<Long>() { // from class: com.example.ui.SchoolViewModel$special$$inlined$map$2
            public Object collect(FlowCollector collector, Continuation $completion) {
                Object collect = $this$map$iv2.collect(new AnonymousClass2(collector), $completion);
                return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
            }

            /* compiled from: Emitters.kt */
            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
            @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n+ 4 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,49:1\n50#2:50\n254#3:51\n1#4:52\n*E\n"})
            /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$2$2  reason: invalid class name */
            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$2$2.dex */
            public static final class AnonymousClass2<T> implements FlowCollector {
                final /* synthetic */ FlowCollector $this_unsafeFlow;

                @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                @DebugMetadata(c = "com.example.ui.SchoolViewModel$special$$inlined$map$2$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$2$2$1  reason: invalid class name */
                /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$2$2$1.dex */
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

                public AnonymousClass2(FlowCollector flowCollector) {
                    this.$this_unsafeFlow = flowCollector;
                }

                /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                /* JADX WARN: Removed duplicated region for block: B:13:0x0043  */
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                    To view partially-correct add '--show-bad-code' argument
                */
                public final java.lang.Object emit(java.lang.Object r20, kotlin.coroutines.Continuation r21) {
                    /*
                        r19 = this;
                        r0 = r19
                        r1 = r21
                        boolean r2 = r1 instanceof com.example.ui.SchoolViewModel$special$$inlined$map$2.AnonymousClass2.AnonymousClass1
                        if (r2 == 0) goto L18
                        r2 = r1
                        com.example.ui.SchoolViewModel$special$$inlined$map$2$2$1 r2 = (com.example.ui.SchoolViewModel$special$$inlined$map$2.AnonymousClass2.AnonymousClass1) r2
                        int r3 = r2.label
                        r4 = -2147483648(0xffffffff80000000, float:-0.0)
                        r3 = r3 & r4
                        if (r3 == 0) goto L18
                        int r3 = r2.label
                        int r3 = r3 - r4
                        r2.label = r3
                        goto L1d
                    L18:
                        com.example.ui.SchoolViewModel$special$$inlined$map$2$2$1 r2 = new com.example.ui.SchoolViewModel$special$$inlined$map$2$2$1
                        r2.<init>(r1)
                    L1d:
                        java.lang.Object r3 = r2.result
                        java.lang.Object r4 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                        int r5 = r2.label
                        switch(r5) {
                            case 0: goto L43;
                            case 1: goto L30;
                            default: goto L28;
                        }
                    L28:
                        java.lang.IllegalStateException r2 = new java.lang.IllegalStateException
                        java.lang.String r3 = "call to 'resume' before 'invoke' with coroutine"
                        r2.<init>(r3)
                        throw r2
                    L30:
                        int r4 = r2.I$0
                        java.lang.Object r5 = r2.L$3
                        kotlinx.coroutines.flow.FlowCollector r5 = (kotlinx.coroutines.flow.FlowCollector) r5
                        java.lang.Object r6 = r2.L$2
                        java.lang.Object r7 = r2.L$1
                        com.example.ui.SchoolViewModel$special$$inlined$map$2$2$1 r7 = (com.example.ui.SchoolViewModel$special$$inlined$map$2.AnonymousClass2.AnonymousClass1) r7
                        java.lang.Object r8 = r2.L$0
                        kotlin.ResultKt.throwOnFailure(r3)
                        goto Lbf
                    L43:
                        kotlin.ResultKt.throwOnFailure(r3)
                        kotlinx.coroutines.flow.FlowCollector r5 = r0.$this_unsafeFlow
                        r7 = r2
                        r6 = r20
                        r8 = 0
                        r9 = r2
                        kotlin.coroutines.Continuation r9 = (kotlin.coroutines.Continuation) r9
                        r10 = r6
                        java.util.List r10 = (java.util.List) r10
                        r11 = 0
                        r12 = r10
                        java.lang.Iterable r12 = (java.lang.Iterable) r12
                        java.util.Iterator r12 = r12.iterator()
                        r13 = 0
                        r15 = r13
                    L5d:
                        boolean r17 = r12.hasNext()
                        if (r17 == 0) goto L73
                        java.lang.Object r17 = r12.next()
                        com.example.data.models.Expense r17 = (com.example.data.models.Expense) r17
                        r18 = 0
                        long r17 = r17.getAmount()
                        long r15 = r15 + r17
                        goto L5d
                    L73:
                        java.lang.Long r12 = kotlin.coroutines.jvm.internal.Boxing.boxLong(r15)
                        r15 = r12
                        java.lang.Number r15 = (java.lang.Number) r15
                        long r15 = r15.longValue()
                        r17 = 0
                        int r18 = (r15 > r13 ? 1 : (r15 == r13 ? 0 : -1))
                        r13 = 1
                        if (r18 <= 0) goto L87
                        r14 = r13
                        goto L88
                    L87:
                        r14 = 0
                    L88:
                        if (r14 == 0) goto L8b
                        goto L8c
                    L8b:
                        r12 = 0
                    L8c:
                        if (r12 == 0) goto L93
                        long r14 = r12.longValue()
                        goto L95
                    L93:
                        r14 = 0
                    L95:
                        java.lang.Long r9 = kotlin.coroutines.jvm.internal.Boxing.boxLong(r14)
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r20)
                        r2.L$0 = r10
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7)
                        r2.L$1 = r10
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r6)
                        r2.L$2 = r10
                        java.lang.Object r10 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r5)
                        r2.L$3 = r10
                        r2.I$0 = r8
                        r2.label = r13
                        java.lang.Object r9 = r5.emit(r9, r2)
                        if (r9 != r4) goto Lbc
                        return r4
                    Lbc:
                        r4 = r8
                        r8 = r20
                    Lbf:
                        kotlin.Unit r4 = kotlin.Unit.INSTANCE
                        return r4
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$special$$inlined$map$2.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                }
            }
        }, ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), 0L);
        Flow $this$flatMapLatest$iv4 = this._currentSchoolId;
        this.hasActiveSubscription = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv4, new SchoolViewModel$special$$inlined$flatMapLatest$4(null, this)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), false);
        Flow $this$flatMapLatest$iv5 = this._currentSchoolId;
        this.isPendingValidation = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv5, new SchoolViewModel$special$$inlined$flatMapLatest$5(null, this)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), false);
        final Flow $this$map$iv3 = this.schoolAccount;
        this.isTrialActive = FlowKt.stateIn(new Flow<Boolean>() { // from class: com.example.ui.SchoolViewModel$special$$inlined$map$3
            public Object collect(FlowCollector collector, Continuation $completion) {
                Object collect = $this$map$iv3.collect(new AnonymousClass2(collector), $completion);
                return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
            }

            /* compiled from: Emitters.kt */
            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
            @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n1#1,49:1\n50#2:50\n266#3,6:51\n*E\n"})
            /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$3$2  reason: invalid class name */
            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$3$2.dex */
            public static final class AnonymousClass2<T> implements FlowCollector {
                final /* synthetic */ FlowCollector $this_unsafeFlow;

                @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                @DebugMetadata(c = "com.example.ui.SchoolViewModel$special$$inlined$map$3$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$3$2$1  reason: invalid class name */
                /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$3$2$1.dex */
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

                public AnonymousClass2(FlowCollector flowCollector) {
                    this.$this_unsafeFlow = flowCollector;
                }

                /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                /* JADX WARN: Removed duplicated region for block: B:13:0x0042  */
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                    To view partially-correct add '--show-bad-code' argument
                */
                public final java.lang.Object emit(java.lang.Object r20, kotlin.coroutines.Continuation r21) {
                    /*
                        r19 = this;
                        r0 = r19
                        r1 = r21
                        boolean r2 = r1 instanceof com.example.ui.SchoolViewModel$special$$inlined$map$3.AnonymousClass2.AnonymousClass1
                        if (r2 == 0) goto L18
                        r2 = r1
                        com.example.ui.SchoolViewModel$special$$inlined$map$3$2$1 r2 = (com.example.ui.SchoolViewModel$special$$inlined$map$3.AnonymousClass2.AnonymousClass1) r2
                        int r3 = r2.label
                        r4 = -2147483648(0xffffffff80000000, float:-0.0)
                        r3 = r3 & r4
                        if (r3 == 0) goto L18
                        int r3 = r2.label
                        int r3 = r3 - r4
                        r2.label = r3
                        goto L1d
                    L18:
                        com.example.ui.SchoolViewModel$special$$inlined$map$3$2$1 r2 = new com.example.ui.SchoolViewModel$special$$inlined$map$3$2$1
                        r2.<init>(r1)
                    L1d:
                        java.lang.Object r3 = r2.result
                        java.lang.Object r4 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                        int r5 = r2.label
                        switch(r5) {
                            case 0: goto L42;
                            case 1: goto L30;
                            default: goto L28;
                        }
                    L28:
                        java.lang.IllegalStateException r2 = new java.lang.IllegalStateException
                        java.lang.String r3 = "call to 'resume' before 'invoke' with coroutine"
                        r2.<init>(r3)
                        throw r2
                    L30:
                        int r4 = r2.I$0
                        java.lang.Object r5 = r2.L$3
                        kotlinx.coroutines.flow.FlowCollector r5 = (kotlinx.coroutines.flow.FlowCollector) r5
                        java.lang.Object r6 = r2.L$2
                        java.lang.Object r7 = r2.L$1
                        com.example.ui.SchoolViewModel$special$$inlined$map$3$2$1 r7 = (com.example.ui.SchoolViewModel$special$$inlined$map$3.AnonymousClass2.AnonymousClass1) r7
                        java.lang.Object r8 = r2.L$0
                        kotlin.ResultKt.throwOnFailure(r3)
                        goto L96
                    L42:
                        kotlin.ResultKt.throwOnFailure(r3)
                        kotlinx.coroutines.flow.FlowCollector r5 = r0.$this_unsafeFlow
                        r7 = r2
                        r6 = r20
                        r8 = 0
                        r9 = r2
                        kotlin.coroutines.Continuation r9 = (kotlin.coroutines.Continuation) r9
                        r10 = r6
                        com.example.data.models.SchoolAccount r10 = (com.example.data.models.SchoolAccount) r10
                        r11 = 0
                        r12 = 1
                        r13 = 0
                        if (r10 != 0) goto L57
                        goto L6b
                    L57:
                        long r14 = java.lang.System.currentTimeMillis()
                        long r16 = r10.getCreatedAt()
                        long r14 = r14 - r16
                        r16 = 7776000000(0x1cf7c5800, double:3.841854462E-314)
                        int r18 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
                        if (r18 >= 0) goto L6b
                        r13 = r12
                    L6b:
                        java.lang.Boolean r13 = kotlin.coroutines.jvm.internal.Boxing.boxBoolean(r13)
                        java.lang.Object r9 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r20)
                        r2.L$0 = r9
                        java.lang.Object r9 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7)
                        r2.L$1 = r9
                        java.lang.Object r9 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r6)
                        r2.L$2 = r9
                        java.lang.Object r9 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r5)
                        r2.L$3 = r9
                        r2.I$0 = r8
                        r2.label = r12
                        java.lang.Object r9 = r5.emit(r13, r2)
                        if (r9 != r4) goto L93
                        return r4
                    L93:
                        r4 = r8
                        r8 = r20
                    L96:
                        kotlin.Unit r4 = kotlin.Unit.INSTANCE
                        return r4
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$special$$inlined$map$3.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                }
            }
        }, ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), false);
        final Flow $this$map$iv4 = this.schoolAccount;
        this.trialDaysRemaining = FlowKt.stateIn(new Flow<Long>() { // from class: com.example.ui.SchoolViewModel$special$$inlined$map$4
            public Object collect(FlowCollector collector, Continuation $completion) {
                Object collect = $this$map$iv4.collect(new AnonymousClass2(collector), $completion);
                return collect == IntrinsicsKt.getCOROUTINE_SUSPENDED() ? collect : Unit.INSTANCE;
            }

            /* compiled from: Emitters.kt */
            @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
            @SourceDebugExtension({"SMAP\nEmitters.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt$unsafeTransform$1$1\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel\n*L\n1#1,49:1\n50#2:50\n275#3,7:51\n*E\n"})
            /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$4$2  reason: invalid class name */
            /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$4$2.dex */
            public static final class AnonymousClass2<T> implements FlowCollector {
                final /* synthetic */ FlowCollector $this_unsafeFlow;

                @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
                @DebugMetadata(c = "com.example.ui.SchoolViewModel$special$$inlined$map$4$2", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0}, l = {50}, m = "emit", n = {"value", "$completion", "value", "$this$map_u24lambda_u245", "$i$a$-unsafeTransform-FlowKt__TransformKt$map$1"}, s = {"L$0", "L$1", "L$2", "L$3", "I$0"})
                /* renamed from: com.example.ui.SchoolViewModel$special$$inlined$map$4$2$1  reason: invalid class name */
                /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$special$$inlined$map$4$2$1.dex */
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

                public AnonymousClass2(FlowCollector flowCollector) {
                    this.$this_unsafeFlow = flowCollector;
                }

                /* JADX WARN: Removed duplicated region for block: B:10:0x0028  */
                /* JADX WARN: Removed duplicated region for block: B:12:0x0030  */
                /* JADX WARN: Removed duplicated region for block: B:13:0x0042  */
                /*
                    Code decompiled incorrectly, please refer to instructions dump.
                    To view partially-correct add '--show-bad-code' argument
                */
                public final java.lang.Object emit(java.lang.Object r23, kotlin.coroutines.Continuation r24) {
                    /*
                        r22 = this;
                        r0 = r22
                        r1 = r24
                        boolean r2 = r1 instanceof com.example.ui.SchoolViewModel$special$$inlined$map$4.AnonymousClass2.AnonymousClass1
                        if (r2 == 0) goto L18
                        r2 = r1
                        com.example.ui.SchoolViewModel$special$$inlined$map$4$2$1 r2 = (com.example.ui.SchoolViewModel$special$$inlined$map$4.AnonymousClass2.AnonymousClass1) r2
                        int r3 = r2.label
                        r4 = -2147483648(0xffffffff80000000, float:-0.0)
                        r3 = r3 & r4
                        if (r3 == 0) goto L18
                        int r3 = r2.label
                        int r3 = r3 - r4
                        r2.label = r3
                        goto L1d
                    L18:
                        com.example.ui.SchoolViewModel$special$$inlined$map$4$2$1 r2 = new com.example.ui.SchoolViewModel$special$$inlined$map$4$2$1
                        r2.<init>(r1)
                    L1d:
                        java.lang.Object r3 = r2.result
                        java.lang.Object r4 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
                        int r5 = r2.label
                        switch(r5) {
                            case 0: goto L42;
                            case 1: goto L30;
                            default: goto L28;
                        }
                    L28:
                        java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
                        java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
                        r0.<init>(r1)
                        throw r0
                    L30:
                        int r4 = r2.I$0
                        java.lang.Object r5 = r2.L$3
                        kotlinx.coroutines.flow.FlowCollector r5 = (kotlinx.coroutines.flow.FlowCollector) r5
                        java.lang.Object r6 = r2.L$2
                        java.lang.Object r7 = r2.L$1
                        com.example.ui.SchoolViewModel$special$$inlined$map$4$2$1 r7 = (com.example.ui.SchoolViewModel$special$$inlined$map$4.AnonymousClass2.AnonymousClass1) r7
                        java.lang.Object r8 = r2.L$0
                        kotlin.ResultKt.throwOnFailure(r3)
                        goto L9d
                    L42:
                        kotlin.ResultKt.throwOnFailure(r3)
                        kotlinx.coroutines.flow.FlowCollector r5 = r0.$this_unsafeFlow
                        r7 = r2
                        r6 = r23
                        r8 = 0
                        r9 = r2
                        kotlin.coroutines.Continuation r9 = (kotlin.coroutines.Continuation) r9
                        r10 = r6
                        com.example.data.models.SchoolAccount r10 = (com.example.data.models.SchoolAccount) r10
                        r11 = 0
                        r12 = 0
                        if (r10 != 0) goto L57
                        goto L71
                    L57:
                        long r14 = java.lang.System.currentTimeMillis()
                        long r16 = r10.getCreatedAt()
                        long r14 = r14 - r16
                        r16 = 7776000000(0x1cf7c5800, double:3.841854462E-314)
                        long r18 = r16 - r14
                        r20 = 86400000(0x5265c00, double:4.2687272E-316)
                        long r0 = r18 / r20
                        long r12 = kotlin.ranges.RangesKt.coerceAtLeast(r0, r12)
                    L71:
                        java.lang.Long r0 = kotlin.coroutines.jvm.internal.Boxing.boxLong(r12)
                        java.lang.Object r1 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r23)
                        r2.L$0 = r1
                        java.lang.Object r1 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r7)
                        r2.L$1 = r1
                        java.lang.Object r1 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r6)
                        r2.L$2 = r1
                        java.lang.Object r1 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r5)
                        r2.L$3 = r1
                        r2.I$0 = r8
                        r1 = 1
                        r2.label = r1
                        java.lang.Object r0 = r5.emit(r0, r2)
                        if (r0 != r4) goto L9a
                        return r4
                    L9a:
                        r4 = r8
                        r8 = r23
                    L9d:
                        kotlin.Unit r0 = kotlin.Unit.INSTANCE
                        return r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$special$$inlined$map$4.AnonymousClass2.emit(java.lang.Object, kotlin.coroutines.Continuation):java.lang.Object");
                }
            }
        }, ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), 0L);
        this.isAppAccessGranted = FlowKt.stateIn(FlowKt.combine(this.schoolAccount, this.isTrialActive, new SchoolViewModel$isAppAccessGranted$1(null)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), false);
        this.balance = FlowKt.stateIn(FlowKt.combine(this.totalCollected, this.totalExpenses, new SchoolViewModel$balance$1(null)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), 0L);
        Flow $this$flatMapLatest$iv6 = this._currentSchoolId;
        this.subjects = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv6, new SchoolViewModel$special$$inlined$flatMapLatest$6(null, this)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), CollectionsKt.emptyList());
        Flow $this$flatMapLatest$iv7 = this._currentSchoolId;
        this.grades = FlowKt.stateIn(FlowKt.transformLatest($this$flatMapLatest$iv7, new SchoolViewModel$special$$inlined$flatMapLatest$7(null, this)), ViewModelKt.getViewModelScope(this), SharingStarted.Companion.WhileSubscribed$default(SharingStarted.Companion, 5000L, 0L, 2, (Object) null), CollectionsKt.emptyList());
        this._pendingOrderId = StateFlowKt.MutableStateFlow((Object) null);
        this.pendingOrderId = FlowKt.asStateFlow(this._pendingOrderId);
        this._adminSchools = StateFlowKt.MutableStateFlow(CollectionsKt.emptyList());
        this.adminSchools = this._adminSchools;
        this._adminError = StateFlowKt.MutableStateFlow((Object) null);
        this.adminError = this._adminError;
    }

    @NotNull
    public final StateFlow<Integer> getCurrentSchoolId() {
        return this.currentSchoolId;
    }

    @NotNull
    public final StateFlow<String> getSchoolName() {
        return this.schoolName;
    }

    @NotNull
    public final StateFlow<String> getSelectedSection() {
        return this.selectedSection;
    }

    @NotNull
    public final StateFlow<String> getUserRole() {
        return this.userRole;
    }

    @NotNull
    public final StateFlow<String> getLoginError() {
        return this.loginError;
    }

    @NotNull
    public final StateFlow<SchoolAccount> getSchoolAccount() {
        return this.schoolAccount;
    }

    @NotNull
    public final StateFlow<String> getSchoolLogoBase64() {
        return this.schoolLogoBase64;
    }

    @NotNull
    public final StateFlow<String> getSelectedSchoolYear() {
        return this.selectedSchoolYear;
    }

    @NotNull
    public final StateFlow<List<DeletionRequest>> getDeletionRequests() {
        return this.deletionRequests;
    }

    @NotNull
    public final StateFlow<List<ClassFee>> getClassFees() {
        return this.classFees;
    }

    public final void setSelectedSchoolYear(@NotNull String year) {
        Intrinsics.checkNotNullParameter(year, "year");
        this._selectedSchoolYear.setValue(year);
        this.sharedPrefs.edit().putString("selected_school_year", year).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isTimestampInSchoolYear(long timestamp, String schoolYear) {
        Integer intOrNull;
        if (Intrinsics.areEqual(schoolYear, "Toutes les années")) {
            return true;
        }
        Iterable $this$map$iv = StringsKt.split$default(schoolYear, new String[]{"-"}, false, 0, 6, (Object) null);
        Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (Object item$iv$iv : $this$map$iv) {
            String it = (String) item$iv$iv;
            destination$iv$iv.add(StringsKt.trim(it).toString());
        }
        List parts = (List) destination$iv$iv;
        if (parts.size() == 2 && (intOrNull = StringsKt.toIntOrNull((String) parts.get(0))) != null) {
            int startYear = intOrNull.intValue();
            Integer intOrNull2 = StringsKt.toIntOrNull((String) parts.get(1));
            if (intOrNull2 != null) {
                int endYear = intOrNull2.intValue();
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(startYear, 8, 1, 0, 0, 0);
                long startMs = cal.getTimeInMillis();
                cal.clear();
                cal.set(endYear, 7, 31, 23, 59, 59);
                cal.set(14, 999);
                long endMs = cal.getTimeInMillis();
                return startMs <= timestamp && timestamp <= endMs;
            }
            return true;
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final long adjustTimestampToSchoolYear(long timestamp, String schoolYear) {
        Integer intOrNull;
        if (Intrinsics.areEqual(schoolYear, "Toutes les années")) {
            return timestamp;
        }
        Iterable $this$map$iv = StringsKt.split$default(schoolYear, new String[]{"-"}, false, 0, 6, (Object) null);
        Collection destination$iv$iv = new ArrayList(CollectionsKt.collectionSizeOrDefault($this$map$iv, 10));
        for (Object item$iv$iv : $this$map$iv) {
            String it = (String) item$iv$iv;
            destination$iv$iv.add(StringsKt.trim(it).toString());
        }
        List parts = (List) destination$iv$iv;
        if (parts.size() != 2 || (intOrNull = StringsKt.toIntOrNull((String) parts.get(0))) == null) {
            return timestamp;
        }
        int startYear = intOrNull.intValue();
        Integer intOrNull2 = StringsKt.toIntOrNull((String) parts.get(1));
        if (intOrNull2 == null) {
            return timestamp;
        }
        int endYear = intOrNull2.intValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int currentMonth = cal.get(2);
        int currentDay = cal.get(5);
        int currentHour = cal.get(11);
        int currentMinute = cal.get(12);
        int currentSecond = cal.get(13);
        int currentMs = cal.get(14);
        cal.clear();
        int targetYear = currentMonth >= 8 ? startYear : endYear;
        cal.set(targetYear, currentMonth, currentDay, currentHour, currentMinute, currentSecond);
        cal.set(14, currentMs);
        return cal.getTimeInMillis();
    }

    private final void saveSession(String email, String role) {
        this.sharedPrefs.edit().putString("last_email", email).putString("last_role", role).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void clearSession() {
        this.sharedPrefs.edit().remove("last_email").remove("last_role").apply();
    }

    private final void restoreSession() {
        String lastEmail = this.sharedPrefs.getString("last_email", null);
        String lastRole = this.sharedPrefs.getString("last_role", null);
        if (lastEmail != null && lastRole != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$restoreSession$1(this, lastRole, lastEmail, null), 3, (Object) null);
            return;
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intrinsics.checkNotNullExpressionValue(auth, "getInstance(...)");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$restoreSession$2(this, currentUser, null), 3, (Object) null);
        }
    }

    public final void setSection(@NotNull String section) {
        Intrinsics.checkNotNullParameter(section, "section");
        this._selectedSection.setValue(section);
    }

    @NotNull
    public final StateFlow<List<Student>> getStudents() {
        return this.students;
    }

    @NotNull
    public final StateFlow<List<Payment>> getPayments() {
        return this.payments;
    }

    @NotNull
    public final StateFlow<Long> getTotalCollected() {
        return this.totalCollected;
    }

    @NotNull
    public final StateFlow<List<Expense>> getExpenses() {
        return this.expenses;
    }

    @NotNull
    public final StateFlow<Long> getTotalExpenses() {
        return this.totalExpenses;
    }

    @NotNull
    public final StateFlow<Boolean> getHasActiveSubscription() {
        return this.hasActiveSubscription;
    }

    @NotNull
    public final StateFlow<Boolean> isPendingValidation() {
        return this.isPendingValidation;
    }

    @NotNull
    public final StateFlow<Boolean> isTrialActive() {
        return this.isTrialActive;
    }

    @NotNull
    public final StateFlow<Long> getTrialDaysRemaining() {
        return this.trialDaysRemaining;
    }

    @NotNull
    public final StateFlow<Boolean> isAppAccessGranted() {
        return this.isAppAccessGranted;
    }

    @NotNull
    public final StateFlow<Long> getBalance() {
        return this.balance;
    }

    @NotNull
    public final StateFlow<List<Subject>> getSubjects() {
        return this.subjects;
    }

    @NotNull
    public final StateFlow<List<StudentGrade>> getGrades() {
        return this.grades;
    }

    public final void insertStudent(@NotNull String firstName, @NotNull String lastName, @NotNull String grade, @NotNull String section, @Nullable String parentWhatsApp, long registrationFee, long reenrollmentFee, @Nullable String photoBase64) {
        Intrinsics.checkNotNullParameter(firstName, "firstName");
        Intrinsics.checkNotNullParameter(lastName, "lastName");
        Intrinsics.checkNotNullParameter(grade, "grade");
        Intrinsics.checkNotNullParameter(section, "section");
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        String year = (String) this._selectedSchoolYear.getValue();
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$insertStudent$1(email, firstName, lastName, grade, section, parentWhatsApp, registrationFee, reenrollmentFee, photoBase64, year, this, schoolId, null), 3, (Object) null);
    }

    public final void updateStudentPhoto(@NotNull Student student, @Nullable String photoBase64) {
        Intrinsics.checkNotNullParameter(student, "student");
        String email = this.sharedPrefs.getString("last_email", null);
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$updateStudentPhoto$1(student, photoBase64, this, email, null), 3, (Object) null);
    }

    public final void setClassFee(@NotNull String grade, long amount) {
        Intrinsics.checkNotNullParameter(grade, "grade");
        String normalizedGrade = SchoolViewModelKt.normalizeGradeName(grade);
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$setClassFee$1(email, normalizedGrade, amount, null), 3, (Object) null);
    }

    public final void setSchoolLogo(@Nullable String base64) {
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        this._schoolLogoBase64.setValue(base64);
        this.sharedPrefs.edit().putString("school_logo_base64", base64).apply();
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$setSchoolLogo$1(this, base64, email, null), 3, (Object) null);
    }

    public final void createDeletionRequest(@NotNull Student student, @NotNull String reason) {
        Intrinsics.checkNotNullParameter(student, "student");
        Intrinsics.checkNotNullParameter(reason, "reason");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$createDeletionRequest$1(email, student, reason, null), 3, (Object) null);
    }

    public final void approveDeletionRequest(@NotNull DeletionRequest request) {
        Intrinsics.checkNotNullParameter(request, "request");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$approveDeletionRequest$1(email, request, this, null), 3, (Object) null);
    }

    public final void rejectDeletionRequest(@NotNull DeletionRequest request, @NotNull String reason) {
        Intrinsics.checkNotNullParameter(request, "request");
        Intrinsics.checkNotNullParameter(reason, "reason");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$rejectDeletionRequest$1(email, request, reason, null), 3, (Object) null);
    }

    public final void dismissDeletionRequest(@NotNull DeletionRequest request) {
        Intrinsics.checkNotNullParameter(request, "request");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$dismissDeletionRequest$1(email, request, null), 3, (Object) null);
    }

    public final void deleteStudentDirectly(@NotNull Student student) {
        Intrinsics.checkNotNullParameter(student, "student");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$deleteStudentDirectly$1(email, student, this, null), 3, (Object) null);
    }

    public static /* synthetic */ void insertPayment$default(SchoolViewModel schoolViewModel, int i, long j, String str, String str2, int i2, Object obj) {
        if ((i2 & 8) != 0) {
            str2 = "Espèces";
        }
        schoolViewModel.insertPayment(i, j, str, str2);
    }

    public final void insertPayment(int studentId, long amount, @NotNull String reason, @NotNull String paymentMethod) {
        Intrinsics.checkNotNullParameter(reason, "reason");
        Intrinsics.checkNotNullParameter(paymentMethod, "paymentMethod");
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$insertPayment$1(this, schoolId, studentId, email, amount, reason, paymentMethod, null), 3, (Object) null);
    }

    public final void deletePayment(int paymentId) {
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$deletePayment$1(this, email, paymentId, null), 3, (Object) null);
    }

    public final void insertExpense(long amount, @NotNull String category, @NotNull String description, @NotNull String section) {
        Intrinsics.checkNotNullParameter(category, "category");
        Intrinsics.checkNotNullParameter(description, "description");
        Intrinsics.checkNotNullParameter(section, "section");
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$insertExpense$1(email, this, amount, section, description, schoolId, null), 3, (Object) null);
    }

    public final void deleteExpense(int expenseId) {
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$deleteExpense$1(this, email, expenseId, null), 3, (Object) null);
    }

    @Nullable
    public final Object hasAccount(@NotNull Continuation<? super Boolean> continuation) {
        return this.repository.hasAccount(continuation);
    }

    private final void startRealtimeSync(final String email, final int schoolId) {
        Log.d("SchoolViewModel", "startRealtimeSync called for email: " + email + ", schoolId: " + schoolId);
        Iterable $this$forEach$iv = this.activeListeners;
        for (Object element$iv : $this$forEach$iv) {
            ListenerRegistration it = (ListenerRegistration) element$iv;
            it.remove();
        }
        this.activeListeners.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
        final DocumentReference schoolDocRef = db.collection("schools").document(email);
        Intrinsics.checkNotNullExpressionValue(schoolDocRef, "document(...)");
        ListenerRegistration schoolListener = schoolDocRef.addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda0
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$24(email, this, (DocumentSnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(schoolListener, "addSnapshotListener(...)");
        this.activeListeners.add(schoolListener);
        ListenerRegistration studentsListener = schoolDocRef.collection("students").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda1
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$25(SchoolViewModel.this, schoolId, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(studentsListener, "addSnapshotListener(...)");
        this.activeListeners.add(studentsListener);
        ListenerRegistration paymentsListener = schoolDocRef.collection("payments").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda2
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$26(SchoolViewModel.this, schoolDocRef, schoolId, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(paymentsListener, "addSnapshotListener(...)");
        this.activeListeners.add(paymentsListener);
        ListenerRegistration expensesListener = schoolDocRef.collection("expenses").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda3
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$27(SchoolViewModel.this, schoolId, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(expensesListener, "addSnapshotListener(...)");
        this.activeListeners.add(expensesListener);
        ListenerRegistration deletionRequestsListener = schoolDocRef.collection("deletionRequests").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda4
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$29(SchoolViewModel.this, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(deletionRequestsListener, "addSnapshotListener(...)");
        this.activeListeners.add(deletionRequestsListener);
        ListenerRegistration classFeesListener = schoolDocRef.collection("classFees").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda5
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$32(SchoolViewModel.this, schoolDocRef, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(classFeesListener, "addSnapshotListener(...)");
        this.activeListeners.add(classFeesListener);
        ListenerRegistration subjectsListener = schoolDocRef.collection("subjects").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda6
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$33(SchoolViewModel.this, schoolId, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(subjectsListener, "addSnapshotListener(...)");
        this.activeListeners.add(subjectsListener);
        ListenerRegistration gradesListener = schoolDocRef.collection("grades").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda7
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.startRealtimeSync$lambda$34(SchoolViewModel.this, schoolId, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
        Intrinsics.checkNotNullExpressionValue(gradesListener, "addSnapshotListener(...)");
        this.activeListeners.add(gradesListener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$24(String $email, SchoolViewModel this$0, DocumentSnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            Log.e("SchoolViewModel", "schoolListener error: " + error.getMessage(), (Throwable) error);
            error.printStackTrace();
            return;
        }
        Log.d("SchoolViewModel", "schoolListener received snapshot");
        if (snapshot != null && snapshot.exists()) {
            String string = snapshot.getString("displayName");
            if (string == null) {
                string = "";
            }
            String displayName = string;
            String string2 = snapshot.getString("schoolName");
            String schoolName = string2 == null ? $email : string2;
            Boolean bool = snapshot.getBoolean("hasActiveSubscription");
            boolean hasActiveSubscription = bool != null ? bool.booleanValue() : false;
            Long l = snapshot.getLong("subscriptionExpiryDate");
            long subscriptionExpiryDate = l != null ? l.longValue() : 0L;
            Boolean bool2 = snapshot.getBoolean("isPendingValidation");
            boolean isPendingValidation = bool2 != null ? bool2.booleanValue() : false;
            String paymentPhoneNumber = snapshot.getString("paymentPhoneNumber");
            String transactionId = snapshot.getString("transactionId");
            String rejectionReason = snapshot.getString("rejectionReason");
            Long createdAt = snapshot.getLong("createdAt");
            String logoBase64 = snapshot.getString("logoBase64");
            this$0._schoolLogoBase64.setValue(logoBase64);
            this$0.sharedPrefs.edit().putString("school_logo_base64", logoBase64).apply();
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$schoolListener$1$1(this$0, $email, createdAt, schoolName, hasActiveSubscription, isPendingValidation, paymentPhoneNumber, transactionId, displayName, rejectionReason, subscriptionExpiryDate, null), 3, (Object) null);
        } else if (snapshot != null && !snapshot.exists()) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$schoolListener$1$2(this$0, $email, null), 3, (Object) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$25(SchoolViewModel this$0, int $schoolId, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$studentsListener$1$1(snapshot, this$0, $schoolId, null), 3, (Object) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$26(SchoolViewModel this$0, DocumentReference $schoolDocRef, int $schoolId, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$paymentsListener$1$1(snapshot, this$0, $schoolDocRef, $schoolId, null), 3, (Object) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$27(SchoolViewModel this$0, int $schoolId, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$expensesListener$1$1(snapshot, this$0, $schoolId, null), 3, (Object) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$29(SchoolViewModel this$0, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        DeletionRequest deletionRequest;
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            Iterable documents = snapshot.getDocuments();
            Intrinsics.checkNotNullExpressionValue(documents, "getDocuments(...)");
            Iterable $this$mapNotNull$iv = documents;
            Collection destination$iv$iv = new ArrayList();
            for (Object element$iv$iv$iv : $this$mapNotNull$iv) {
                DocumentSnapshot doc = (DocumentSnapshot) element$iv$iv$iv;
                String studentRemoteId = doc.getString("studentRemoteId");
                if (studentRemoteId == null) {
                    deletionRequest = null;
                } else {
                    String string = doc.getString("studentName");
                    String studentName = string == null ? "" : string;
                    String string2 = doc.getString("grade");
                    String grade = string2 == null ? "" : string2;
                    String string3 = doc.getString("section");
                    String section = string3 == null ? "" : string3;
                    String string4 = doc.getString("reason");
                    String reason = string4 == null ? "" : string4;
                    String string5 = doc.getString("requestedBy");
                    String requestedBy = string5 == null ? "" : string5;
                    Long l = doc.getLong("requestedAt");
                    long requestedAt = l != null ? l.longValue() : 0L;
                    String string6 = doc.getString("status");
                    if (string6 == null) {
                        string6 = "PENDING";
                    }
                    String status = string6;
                    String string7 = doc.getString("rejectionReason");
                    String rejectionReason = string7 == null ? "" : string7;
                    String id = doc.getId();
                    Intrinsics.checkNotNullExpressionValue(id, "getId(...)");
                    deletionRequest = new DeletionRequest(id, studentRemoteId, studentName, grade, section, reason, requestedBy, requestedAt, status, rejectionReason);
                }
                if (deletionRequest != null) {
                    destination$iv$iv.add(deletionRequest);
                }
            }
            List requestsList = (List) destination$iv$iv;
            this$0._deletionRequests.setValue(requestsList);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$32(SchoolViewModel this$0, DocumentReference $schoolDocRef, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            Iterable documents = snapshot.getDocuments();
            Intrinsics.checkNotNullExpressionValue(documents, "getDocuments(...)");
            Iterable $this$mapNotNull$iv = documents;
            Collection destination$iv$iv = new ArrayList();
            for (Object element$iv$iv$iv : $this$mapNotNull$iv) {
                DocumentSnapshot doc = (DocumentSnapshot) element$iv$iv$iv;
                String string = doc.getString("grade");
                if (string == null) {
                    string = doc.getId();
                    Intrinsics.checkNotNullExpressionValue(string, "getId(...)");
                }
                String grade = string;
                Long l = doc.getLong("feeAmount");
                long feeAmount = l != null ? l.longValue() : 0L;
                String normalizedGrade = SchoolViewModelKt.normalizeGradeName(grade);
                if (!Intrinsics.areEqual(normalizedGrade, grade)) {
                    BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$classFeesListener$1$feesList$1$1($schoolDocRef, normalizedGrade, feeAmount, doc, null), 3, (Object) null);
                    grade = normalizedGrade;
                }
                destination$iv$iv.add(new ClassFee(grade, feeAmount));
            }
            Iterable feesList = (List) destination$iv$iv;
            MutableStateFlow<List<ClassFee>> mutableStateFlow = this$0._classFees;
            Iterable $this$distinctBy$iv = feesList;
            HashSet set$iv = new HashSet();
            ArrayList list$iv = new ArrayList();
            for (Object e$iv : $this$distinctBy$iv) {
                ClassFee it = (ClassFee) e$iv;
                if (set$iv.add(it.getGrade())) {
                    list$iv.add(e$iv);
                }
            }
            ArrayList $this$distinctBy$iv2 = list$iv;
            mutableStateFlow.setValue($this$distinctBy$iv2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$33(SchoolViewModel this$0, int $schoolId, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$subjectsListener$1$1(snapshot, this$0, $schoolId, null), 3, (Object) null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startRealtimeSync$lambda$34(SchoolViewModel this$0, int $schoolId, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error != null) {
            error.printStackTrace();
        } else if (snapshot != null) {
            BuildersKt.launch$default(ViewModelKt.getViewModelScope(this$0), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$startRealtimeSync$gradesListener$1$1(snapshot, this$0, $schoolId, null), 3, (Object) null);
        }
    }

    public static /* synthetic */ void insertSubject$default(SchoolViewModel schoolViewModel, String str, String str2, String str3, int i, float f, int i2, Object obj) {
        if ((i2 & 16) != 0) {
            f = 20.0f;
        }
        schoolViewModel.insertSubject(str, str2, str3, i, f);
    }

    public final void insertSubject(@NotNull String section, @NotNull String grade, @NotNull String name, int coefficient, float maxScore) {
        Intrinsics.checkNotNullParameter(section, "section");
        Intrinsics.checkNotNullParameter(grade, "grade");
        Intrinsics.checkNotNullParameter(name, "name");
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$insertSubject$1(email, section, grade, name, coefficient, maxScore, this, schoolId, null), 3, (Object) null);
    }

    public final void deleteSubject(@NotNull Subject subject) {
        Intrinsics.checkNotNullParameter(subject, "subject");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$deleteSubject$1(subject, email, this, null), 3, (Object) null);
    }

    public final void seedDefaultSubjects(@NotNull String section, @NotNull String grade) {
        Intrinsics.checkNotNullParameter(section, "section");
        Intrinsics.checkNotNullParameter(grade, "grade");
        boolean equals = StringsKt.equals(section, "LA MATERNELLE", true);
        Float valueOf = Float.valueOf(20.0f);
        Iterable defaultList = equals ? CollectionsKt.listOf(new Triple[]{new Triple("Graphisme & Écriture", 1, valueOf), new Triple("Éveil & Langage", 1, valueOf), new Triple("Calcul & Manipulation", 1, valueOf), new Triple("Activités Manuelles", 1, valueOf), new Triple("Psychomotricité", 1, valueOf)}) : StringsKt.equals(section, "LE PRIMAIRE", true) ? (StringsKt.contains$default(grade, "1", false, 2, (Object) null) || StringsKt.contains$default(grade, "2", false, 2, (Object) null) || StringsKt.contains(grade, "CP", true)) ? CollectionsKt.listOf(new Triple[]{new Triple("Lecture", 1, Float.valueOf(10.0f)), new Triple("Langage", 1, Float.valueOf(10.0f)), new Triple("Ecriture", 1, Float.valueOf(10.0f)), new Triple("Calcul", 1, Float.valueOf(10.0f)), new Triple("Exercice Sensoriels", 1, Float.valueOf(10.0f)), new Triple("Dessin", 1, Float.valueOf(10.0f)), new Triple("Récitation/Chant", 1, Float.valueOf(10.0f))}) : CollectionsKt.listOf(new Triple[]{new Triple("Lecture", 1, Float.valueOf(10.0f)), new Triple("Dictée/questions", 1, Float.valueOf(10.0f)), new Triple("Expression Ecrite/Redaction", 1, Float.valueOf(10.0f)), new Triple("Ecriture", 1, Float.valueOf(10.0f)), new Triple("Calcul Ecrit", 1, Float.valueOf(10.0f)), new Triple("Histoire", 1, Float.valueOf(10.0f)), new Triple("Science Observation", 1, Float.valueOf(10.0f)), new Triple("Geographie", 1, Float.valueOf(10.0f)), new Triple("Dessin", 1, Float.valueOf(10.0f)), new Triple("Récitation/Chant", 1, Float.valueOf(10.0f)), new Triple("Instruction Civique", 1, Float.valueOf(10.0f))}) : (StringsKt.contains(section, "COLLEGE", true) || StringsKt.contains(section, "COLLÈGE", true)) ? CollectionsKt.listOf(new Triple[]{new Triple("DICTEE/QUESTION", 2, valueOf), new Triple("REDACTION", 1, valueOf), new Triple("HISTOIRE", 1, valueOf), new Triple("GEOGRAPHIE", 1, valueOf), new Triple("MATHS", 2, valueOf), new Triple("BIOLOGIE", 1, valueOf), new Triple("PHYSIQUES", 1, valueOf), new Triple("CHIMIE", 1, valueOf), new Triple("E.C.M", 1, valueOf), new Triple("ANGLAIS", 1, valueOf)}) : (StringsKt.equals(section, "LE LYCÉE", true) || StringsKt.equals(section, "LE LYCEE", true)) ? CollectionsKt.listOf(new Triple[]{new Triple("Mathématiques", 5, valueOf), new Triple("Physique", 4, valueOf), new Triple("Chimie", 3, valueOf), new Triple("Français / Philosophie", 3, valueOf), new Triple("SVT", 3, valueOf), new Triple("Anglais", 2, valueOf), new Triple("Histoire-Géographie", 2, valueOf)}) : CollectionsKt.emptyList();
        Iterable $this$forEach$iv = defaultList;
        for (Object element$iv : $this$forEach$iv) {
            Triple triple = (Triple) element$iv;
            String name = (String) triple.component1();
            int coeff = ((Number) triple.component2()).intValue();
            float max = ((Number) triple.component3()).floatValue();
            insertSubject(section, grade, name, coeff, max);
        }
    }

    public static /* synthetic */ void saveGrade$default(SchoolViewModel schoolViewModel, int i, String str, int i2, String str2, String str3, Float f, Float f2, String str4, int i3, Object obj) {
        String str5;
        if ((i3 & 128) == 0) {
            str5 = str4;
        } else {
            str5 = null;
        }
        schoolViewModel.saveGrade(i, str, i2, str2, str3, f, f2, str5);
    }

    public final void saveGrade(int studentId, @NotNull String studentRemoteId, int subjectId, @NotNull String subjectRemoteId, @NotNull String term, @Nullable Float evaluationScore, @Nullable Float examScore, @Nullable String comment) {
        Intrinsics.checkNotNullParameter(studentRemoteId, "studentRemoteId");
        Intrinsics.checkNotNullParameter(subjectRemoteId, "subjectRemoteId");
        Intrinsics.checkNotNullParameter(term, "term");
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$saveGrade$1(this, email, studentId, studentRemoteId, subjectId, subjectRemoteId, term, evaluationScore, examScore, comment, schoolId, null), 3, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't wrap try/catch for region: R(7:1|(2:3|(4:5|6|7|8))|247|6|7|8|(1:(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x008e, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x008f, code lost:
        r17 = true;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0032  */
    /* JADX WARN: Removed duplicated region for block: B:124:0x048c A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:125:0x048d  */
    /* JADX WARN: Removed duplicated region for block: B:12:0x003a  */
    /* JADX WARN: Removed duplicated region for block: B:142:0x04c5  */
    /* JADX WARN: Removed duplicated region for block: B:150:0x04f3  */
    /* JADX WARN: Removed duplicated region for block: B:153:0x04fe  */
    /* JADX WARN: Removed duplicated region for block: B:159:0x0557 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0064  */
    /* JADX WARN: Removed duplicated region for block: B:160:0x0558  */
    /* JADX WARN: Removed duplicated region for block: B:162:0x055d  */
    /* JADX WARN: Removed duplicated region for block: B:165:0x056e  */
    /* JADX WARN: Removed duplicated region for block: B:199:0x063b A[Catch: Exception -> 0x0695, TryCatch #5 {Exception -> 0x0695, blocks: (B:208:0x06e5, B:202:0x068b, B:197:0x0635, B:199:0x063b, B:205:0x0697), top: B:231:0x0635 }] */
    /* JADX WARN: Removed duplicated region for block: B:205:0x0697 A[Catch: Exception -> 0x0695, TryCatch #5 {Exception -> 0x0695, blocks: (B:208:0x06e5, B:202:0x068b, B:197:0x0635, B:199:0x063b, B:205:0x0697), top: B:231:0x0635 }] */
    /* JADX WARN: Removed duplicated region for block: B:20:0x0095  */
    /* JADX WARN: Removed duplicated region for block: B:214:0x0722  */
    /* JADX WARN: Removed duplicated region for block: B:215:0x0725  */
    /* JADX WARN: Removed duplicated region for block: B:217:0x0729  */
    /* JADX WARN: Removed duplicated region for block: B:219:0x074b  */
    /* JADX WARN: Removed duplicated region for block: B:250:0x0279 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:25:0x00cb  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x00e6  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x00fb  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x0198  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x021d  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x023c  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x0254  */
    /* JADX WARN: Removed duplicated region for block: B:55:0x02b5 A[Catch: Exception -> 0x04d6, TryCatch #4 {Exception -> 0x04d6, blocks: (B:53:0x02ad, B:55:0x02b5, B:59:0x02c2, B:63:0x02ce, B:68:0x02dd, B:72:0x02ea, B:77:0x02f9, B:82:0x0308, B:88:0x0319, B:93:0x033c, B:92:0x0338), top: B:229:0x02ad }] */
    /* JADX WARN: Type inference failed for: r10v32 */
    /* JADX WARN: Type inference failed for: r5v2 */
    /* JADX WARN: Type inference failed for: r5v47 */
    /* JADX WARN: Type inference failed for: r5v52 */
    /* JADX WARN: Type inference failed for: r6v50 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object syncAccount(java.lang.String r46, kotlin.coroutines.Continuation<? super kotlin.Unit> r47) {
        /*
            Method dump skipped, instructions count: 1896
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel.syncAccount(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX WARN: Removed duplicated region for block: B:101:0x04c9 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:102:0x04ca  */
    /* JADX WARN: Removed duplicated region for block: B:105:0x04d1 A[Catch: Exception -> 0x0517, TryCatch #7 {Exception -> 0x0517, blocks: (B:103:0x04cd, B:105:0x04d1, B:110:0x04f5, B:111:0x04fc), top: B:162:0x04cd }] */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0033  */
    /* JADX WARN: Removed duplicated region for block: B:113:0x050a  */
    /* JADX WARN: Removed duplicated region for block: B:12:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:131:0x056e  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x007f  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x00c6  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0118  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x0154  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x019d  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x01d3  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x023b A[Catch: Exception -> 0x057e, TRY_LEAVE, TryCatch #13 {Exception -> 0x057e, blocks: (B:55:0x0231, B:57:0x023b), top: B:174:0x0231 }] */
    /* JADX WARN: Removed duplicated region for block: B:70:0x02da A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:89:0x0400 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:90:0x0401  */
    /* JADX WARN: Removed duplicated region for block: B:95:0x046b A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:96:0x046c  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object registerSchool(@org.jetbrains.annotations.NotNull java.lang.String r30, @org.jetbrains.annotations.NotNull java.lang.String r31, @org.jetbrains.annotations.NotNull java.lang.String r32, @org.jetbrains.annotations.NotNull java.lang.String r33, @org.jetbrains.annotations.NotNull java.lang.String r34, @org.jetbrains.annotations.NotNull java.lang.String r35, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r36) {
        /*
            Method dump skipped, instructions count: 1554
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel.registerSchool(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Can't wrap try/catch for region: R(7:1|(2:3|(4:5|6|7|8))|118|6|7|8|(1:(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x009f, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x0140, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x0141, code lost:
        r8 = r1;
        r1 = r16;
        r11 = r10;
        r10 = r14;
        r14 = r13;
        r13 = r12;
        r12 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:94:0x0376, code lost:
        return r6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:95:0x0377, code lost:
        r7 = r8;
        r8 = r10;
        r9 = r11;
        r10 = r12;
        r11 = r13;
        r12 = r14;
        r13 = r15;
     */
    /* JADX WARN: Not initialized variable reg: 10, insn: 0x0146: MOVE  (r11 I:??[OBJECT, ARRAY] A[D('cleanEmail' java.lang.String)]) = (r10 I:??[OBJECT, ARRAY] A[D('financierEmail' java.lang.String)]), block:B:34:0x0141 */
    /* JADX WARN: Not initialized variable reg: 11, insn: 0x0144: MOVE  (r20 I:??[OBJECT, ARRAY]) = (r11 I:??[OBJECT, ARRAY] A[D('cleanEmail' java.lang.String)]), block:B:34:0x0141 */
    /* JADX WARN: Not initialized variable reg: 12, insn: 0x0149: MOVE  (r13 I:??[OBJECT, ARRAY] A[D('currentAuthUser' com.google.firebase.auth.FirebaseUser)]) = (r12 I:??[OBJECT, ARRAY] A[D('currentEmail' java.lang.String)]), block:B:34:0x0141 */
    /* JADX WARN: Not initialized variable reg: 13, insn: 0x0148: MOVE  (r14 I:??[OBJECT, ARRAY] A[D('auth' com.google.firebase.auth.FirebaseAuth)]) = (r13 I:??[OBJECT, ARRAY] A[D('currentAuthUser' com.google.firebase.auth.FirebaseUser)]), block:B:34:0x0141 */
    /* JADX WARN: Not initialized variable reg: 14, insn: 0x0147: MOVE  (r10 I:??[OBJECT, ARRAY] A[D('financierEmail' java.lang.String)]) = (r14 I:??[OBJECT, ARRAY] A[D('auth' com.google.firebase.auth.FirebaseAuth)]), block:B:34:0x0141 */
    /* JADX WARN: Not initialized variable reg: 16, insn: 0x0142: MOVE  (r1 I:??[OBJECT, ARRAY] A[D('financierPassword' java.lang.String)]) = (r16 I:??[OBJECT, ARRAY] A[D('schoolEmail' java.lang.String)]), block:B:34:0x0141 */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0031  */
    /* JADX WARN: Removed duplicated region for block: B:12:0x0039  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0072  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x00a2  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x00dd  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x010f  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x014e  */
    /* JADX WARN: Removed duplicated region for block: B:52:0x01f9 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:65:0x025f A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:66:0x0260  */
    /* JADX WARN: Removed duplicated region for block: B:76:0x02ab A[Catch: Exception -> 0x009f, TryCatch #2 {Exception -> 0x009f, blocks: (B:18:0x009a, B:74:0x02a6, B:76:0x02ab, B:78:0x02b1), top: B:102:0x002e }] */
    /* JADX WARN: Removed duplicated region for block: B:94:0x0376 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:95:0x0377  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object syncFinancierAuthAccount(java.lang.String r23, java.lang.String r24, java.lang.String r25, kotlin.coroutines.Continuation<? super kotlin.Unit> r26) {
        /*
            Method dump skipped, instructions count: 942
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel.syncFinancierAuthAccount(java.lang.String, java.lang.String, java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:384:0x173b
        	at jadx.core.dex.visitors.blocks.BlockProcessor.checkForUnreachableBlocks(BlockProcessor.java:81)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.processBlocksTree(BlockProcessor.java:47)
        	at jadx.core.dex.visitors.blocks.BlockProcessor.visit(BlockProcessor.java:39)
        */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object login(@org.jetbrains.annotations.NotNull java.lang.String r119, @org.jetbrains.annotations.NotNull java.lang.String r120, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r121) {
        /*
            Method dump skipped, instructions count: 14072
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel.login(java.lang.String, java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateStudentFinancialsInRTDB(int schoolId, int studentId) {
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$updateStudentFinancialsInRTDB$1(this, schoolId, studentId, null), 3, (Object) null);
    }

    public final void syncStudentAcademicsToRTDB(int schoolId, int studentId, @NotNull String term) {
        Intrinsics.checkNotNullParameter(term, "term");
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$syncStudentAcademicsToRTDB$1(this, schoolId, term, studentId, null), 3, (Object) null);
    }

    public final void logout() {
        Iterable $this$forEach$iv = this.activeListeners;
        for (Object element$iv : $this$forEach$iv) {
            ListenerRegistration it = (ListenerRegistration) element$iv;
            it.remove();
        }
        this.activeListeners.clear();
        ListenerRegistration listenerRegistration = this.adminListener;
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        this.adminListener = null;
        this._currentSchoolId.setValue((Object) null);
        this._schoolName.setValue((Object) null);
        this._schoolLogoBase64.setValue((Object) null);
        this._userRole.setValue((Object) null);
        this.sharedPrefs.edit().remove("school_logo_base64").apply();
        clearSession();
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void activateSubscription() {
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$activateSubscription$1(email, this, schoolId, null), 3, (Object) null);
    }

    @NotNull
    /* renamed from: getPendingOrderId  reason: collision with other method in class */
    public final StateFlow<String> m4getPendingOrderId() {
        return this.pendingOrderId;
    }

    public final void savePendingOrderId(@NotNull String orderId) {
        Intrinsics.checkNotNullParameter(orderId, "orderId");
        this.sharedPrefs.edit().putString("pending_order_id", orderId).apply();
        this._pendingOrderId.setValue(orderId);
    }

    @Nullable
    public final String getPendingOrderId() {
        String orderId = this.sharedPrefs.getString("pending_order_id", null);
        this._pendingOrderId.setValue(orderId);
        return orderId;
    }

    public final void clearPendingOrderId() {
        this.sharedPrefs.edit().remove("pending_order_id").apply();
        this._pendingOrderId.setValue((Object) null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static /* synthetic */ void checkPendingPaymentStatus$default(SchoolViewModel schoolViewModel, Function1 function1, int i, Object obj) {
        if ((i & 1) != 0) {
            function1 = null;
        }
        schoolViewModel.checkPendingPaymentStatus(function1);
    }

    public final void checkPendingPaymentStatus(@Nullable Function1<? super String, Unit> function1) {
        String orderId = getPendingOrderId();
        if (orderId == null) {
            if (function1 != null) {
                function1.invoke("NONE");
                return;
            }
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$checkPendingPaymentStatus$1(orderId, this, function1, null), 3, (Object) null);
    }

    public final void submitSubscriptionRequest(@NotNull String phoneNumber, @NotNull String transactionId) {
        Intrinsics.checkNotNullParameter(phoneNumber, "phoneNumber");
        Intrinsics.checkNotNullParameter(transactionId, "transactionId");
        Integer num = (Integer) this._currentSchoolId.getValue();
        if (num == null) {
            return;
        }
        int schoolId = num.intValue();
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$submitSubscriptionRequest$1(email, phoneNumber, transactionId, this, schoolId, null), 3, (Object) null);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(9:1|(2:3|(7:5|6|7|8|14|15|16))|25|6|7|8|14|15|16) */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x0068, code lost:
        r2 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x0069, code lost:
        r2.printStackTrace();
        r4 = false;
     */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0025  */
    /* JADX WARN: Removed duplicated region for block: B:12:0x002d  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x003a  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object sendPasswordResetEmail(@org.jetbrains.annotations.NotNull java.lang.String r8, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r9) {
        /*
            r7 = this;
            boolean r0 = r9 instanceof com.example.ui.SchoolViewModel$sendPasswordResetEmail$1
            if (r0 == 0) goto L14
            r0 = r9
            com.example.ui.SchoolViewModel$sendPasswordResetEmail$1 r0 = (com.example.ui.SchoolViewModel$sendPasswordResetEmail$1) r0
            int r1 = r0.label
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
            r1 = r1 & r2
            if (r1 == 0) goto L14
            int r1 = r0.label
            int r1 = r1 - r2
            r0.label = r1
            goto L19
        L14:
            com.example.ui.SchoolViewModel$sendPasswordResetEmail$1 r0 = new com.example.ui.SchoolViewModel$sendPasswordResetEmail$1
            r0.<init>(r7, r9)
        L19:
            java.lang.Object r1 = r0.result
            java.lang.Object r2 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r3 = r0.label
            r4 = 1
            switch(r3) {
                case 0: goto L3a;
                case 1: goto L2d;
                default: goto L25;
            }
        L25:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L2d:
            java.lang.Object r2 = r0.L$1
            com.google.firebase.auth.FirebaseAuth r2 = (com.google.firebase.auth.FirebaseAuth) r2
            java.lang.Object r3 = r0.L$0
            r8 = r3
            java.lang.String r8 = (java.lang.String) r8
            kotlin.ResultKt.throwOnFailure(r1)     // Catch: java.lang.Exception -> L68
            goto L66
        L3a:
            kotlin.ResultKt.throwOnFailure(r1)
            com.google.firebase.auth.FirebaseAuth r3 = com.google.firebase.auth.FirebaseAuth.getInstance()     // Catch: java.lang.Exception -> L68
            java.lang.String r5 = "getInstance(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r3, r5)     // Catch: java.lang.Exception -> L68
            com.google.android.gms.tasks.Task r5 = r3.sendPasswordResetEmail(r8)     // Catch: java.lang.Exception -> L68
            java.lang.String r6 = "sendPasswordResetEmail(...)"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r5, r6)     // Catch: java.lang.Exception -> L68
            java.lang.Object r6 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r8)     // Catch: java.lang.Exception -> L68
            r0.L$0 = r6     // Catch: java.lang.Exception -> L68
            java.lang.Object r6 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r3)     // Catch: java.lang.Exception -> L68
            r0.L$1 = r6     // Catch: java.lang.Exception -> L68
            r0.label = r4     // Catch: java.lang.Exception -> L68
            java.lang.Object r5 = kotlinx.coroutines.tasks.TasksKt.await(r5, r0)     // Catch: java.lang.Exception -> L68
            if (r5 != r2) goto L65
            return r2
        L65:
            r2 = r3
        L66:
            goto L6d
        L68:
            r2 = move-exception
            r2.printStackTrace()
            r4 = 0
        L6d:
            java.lang.Boolean r2 = kotlin.coroutines.jvm.internal.Boxing.boxBoolean(r4)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel.sendPasswordResetEmail(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    public final void updateFinancierPassword(@NotNull String newPassword) {
        Intrinsics.checkNotNullParameter(newPassword, "newPassword");
        String email = this.sharedPrefs.getString("last_email", null);
        if (email == null) {
            return;
        }
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$updateFinancierPassword$1(email, newPassword, this, null), 3, (Object) null);
    }

    @NotNull
    public final StateFlow<List<SchoolAdminItem>> getAdminSchools() {
        return this.adminSchools;
    }

    @NotNull
    public final StateFlow<String> getAdminError() {
        return this.adminError;
    }

    public final void loadAdminSchools() {
        ListenerRegistration listenerRegistration = this.adminListener;
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        this._adminError.setValue((Object) null);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Intrinsics.checkNotNullExpressionValue(db, "getInstance(...)");
        this.adminListener = db.collection("schools").addSnapshotListener(new EventListener() { // from class: com.example.ui.SchoolViewModel$$ExternalSyntheticLambda8
            public final void onEvent(Object obj, FirebaseFirestoreException firebaseFirestoreException) {
                SchoolViewModel.loadAdminSchools$lambda$47(SchoolViewModel.this, (QuerySnapshot) obj, firebaseFirestoreException);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void loadAdminSchools$lambda$47(SchoolViewModel this$0, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        boolean z;
        Iterable $this$mapNotNullTo$iv$iv;
        boolean z2;
        SchoolAdminItem schoolAdminItem;
        if (error != null) {
            error.printStackTrace();
            this$0._adminError.setValue("Erreur de chargement: " + error.getMessage());
        } else if (snapshot != null) {
            Iterable documents = snapshot.getDocuments();
            Intrinsics.checkNotNullExpressionValue(documents, "getDocuments(...)");
            Iterable $this$mapNotNull$iv = documents;
            boolean z3 = false;
            Collection destination$iv$iv = new ArrayList();
            Iterable $this$mapNotNullTo$iv$iv2 = $this$mapNotNull$iv;
            boolean z4 = false;
            for (Object element$iv$iv$iv : $this$mapNotNullTo$iv$iv2) {
                DocumentSnapshot doc = (DocumentSnapshot) element$iv$iv$iv;
                String email = doc.getId();
                Intrinsics.checkNotNullExpressionValue(email, "getId(...)");
                Iterable $this$mapNotNull$iv2 = $this$mapNotNull$iv;
                String lowerCase = StringsKt.trim(email).toString().toLowerCase(Locale.ROOT);
                Intrinsics.checkNotNullExpressionValue(lowerCase, "toLowerCase(...)");
                if (Intrinsics.areEqual(lowerCase, "benjamintolno7@gmail.com")) {
                    schoolAdminItem = null;
                    z = z3;
                    $this$mapNotNullTo$iv$iv = $this$mapNotNullTo$iv$iv2;
                    z2 = z4;
                } else {
                    String string = doc.getString("schoolName");
                    String schoolName = string == null ? email : string;
                    String string2 = doc.getString("displayName");
                    String displayName = string2 == null ? "" : string2;
                    Boolean bool = doc.getBoolean("hasActiveSubscription");
                    boolean hasActiveSubscription = bool != null ? bool.booleanValue() : false;
                    Long l = doc.getLong("subscriptionExpiryDate");
                    long subscriptionExpiryDate = l != null ? l.longValue() : 0L;
                    Boolean bool2 = doc.getBoolean("isPendingValidation");
                    boolean isPendingValidation = bool2 != null ? bool2.booleanValue() : false;
                    String paymentPhoneNumber = doc.getString("paymentPhoneNumber");
                    z = z3;
                    String transactionId = doc.getString("transactionId");
                    $this$mapNotNullTo$iv$iv = $this$mapNotNullTo$iv$iv2;
                    String rejectionReason = doc.getString("rejectionReason");
                    z2 = z4;
                    Long l2 = doc.getLong("createdAt");
                    long createdAt = l2 != null ? l2.longValue() : System.currentTimeMillis();
                    String string3 = doc.getString("address");
                    String address = string3 == null ? "" : string3;
                    String string4 = doc.getString("founderPhone");
                    String founderPhone = string4 == null ? "" : string4;
                    schoolAdminItem = new SchoolAdminItem(email, schoolName, displayName, hasActiveSubscription, subscriptionExpiryDate, isPendingValidation, paymentPhoneNumber, transactionId, rejectionReason, createdAt, address, founderPhone);
                }
                if (schoolAdminItem != null) {
                    destination$iv$iv.add(schoolAdminItem);
                }
                $this$mapNotNull$iv = $this$mapNotNull$iv2;
                z3 = z;
                $this$mapNotNullTo$iv$iv2 = $this$mapNotNullTo$iv$iv;
                z4 = z2;
            }
            Iterable list = (List) destination$iv$iv;
            Iterable $this$sortedByDescending$iv = list;
            this$0._adminSchools.setValue(CollectionsKt.sortedWith($this$sortedByDescending$iv, new Comparator() { // from class: com.example.ui.SchoolViewModel$loadAdminSchools$lambda$47$$inlined$sortedByDescending$1
                @Override // java.util.Comparator
                public final int compare(T t, T t2) {
                    SchoolAdminItem it = (SchoolAdminItem) t2;
                    SchoolAdminItem it2 = (SchoolAdminItem) t;
                    return ComparisonsKt.compareValues(Long.valueOf(it.getCreatedAt()), Long.valueOf(it2.getCreatedAt()));
                }
            }));
        }
    }

    public final void approveSchoolSubscription(@NotNull String email) {
        Intrinsics.checkNotNullParameter(email, "email");
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$approveSchoolSubscription$1(email, this, null), 3, (Object) null);
    }

    public final void deleteSchoolAccount(@NotNull String email) {
        Intrinsics.checkNotNullParameter(email, "email");
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$deleteSchoolAccount$1(email, this, null), 3, (Object) null);
    }

    public final void rejectSchoolSubscription(@NotNull String email, @NotNull String reason) {
        Intrinsics.checkNotNullParameter(email, "email");
        Intrinsics.checkNotNullParameter(reason, "reason");
        BuildersKt.launch$default(ViewModelKt.getViewModelScope(this), (CoroutineContext) null, (CoroutineStart) null, new SchoolViewModel$rejectSchoolSubscription$1(email, reason, this, null), 3, (Object) null);
    }
}
