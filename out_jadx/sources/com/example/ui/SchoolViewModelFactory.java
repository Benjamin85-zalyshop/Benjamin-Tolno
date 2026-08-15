package com.example.ui;

import android.content.Context;
import androidx.compose.runtime.internal.StabilityInferred;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;
import com.example.data.repository.SchoolRepository;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
/* compiled from: SchoolViewModel.kt */
@StabilityInferred(parameters = 0)
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0004\b\u0006\u0010\u0007J%\u0010\b\u001a\u0002H\t\"\b\b\u0000\u0010\t*\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u0002H\t0\fH\u0016¢\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u000e"}, d2 = {"Lcom/example/ui/SchoolViewModelFactory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "repository", "Lcom/example/data/repository/SchoolRepository;", "context", "Landroid/content/Context;", "<init>", "(Lcom/example/data/repository/SchoolRepository;Landroid/content/Context;)V", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_debug"}, k = 1, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModelFactory.dex */
public final class SchoolViewModelFactory implements ViewModelProvider.Factory {
    public static final int $stable = 8;
    @NotNull
    private final Context context;
    @NotNull
    private final SchoolRepository repository;

    public SchoolViewModelFactory(@NotNull SchoolRepository repository, @NotNull Context context) {
        Intrinsics.checkNotNullParameter(repository, "repository");
        Intrinsics.checkNotNullParameter(context, "context");
        this.repository = repository;
        this.context = context;
    }

    @NotNull
    public <T extends ViewModel> T create(@NotNull Class<T> cls, @NotNull CreationExtras extras) {
        return (T) super.create(cls, extras);
    }

    @NotNull
    public <T extends ViewModel> T create(@NotNull KClass<T> kClass, @NotNull CreationExtras extras) {
        return (T) super.create(kClass, extras);
    }

    @NotNull
    public <T extends ViewModel> T create(@NotNull Class<T> cls) {
        Intrinsics.checkNotNullParameter(cls, "modelClass");
        if (cls.isAssignableFrom(SchoolViewModel.class)) {
            return new SchoolViewModel(this.repository, this.context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
