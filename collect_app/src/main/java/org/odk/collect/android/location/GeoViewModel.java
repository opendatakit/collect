package org.odk.collect.android.location;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.architecture.rx.RxMvvmViewModel;
import org.odk.collect.android.location.model.ZoomData;
import org.odk.collect.android.location.usecases.InfoText;
import org.odk.collect.android.location.usecases.InitialState;
import org.odk.collect.android.location.usecases.SelectedLocation;
import org.odk.collect.android.location.usecases.StatusText;
import org.odk.collect.android.location.usecases.WatchPosition;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;


public class GeoViewModel extends RxMvvmViewModel {

    @NonNull
    private final InitialState initialState;

    // Inputs:
    @NonNull
    private final WatchPosition watchPosition;

    @NonNull
    private final SelectedLocation selectedLocation;

    @NonNull
    private final InfoText infoText;

    @NonNull
    private final StatusText statusText;

    // Outputs:
    @NonNull
    private final Observable<Integer> textVisibility;

    @NonNull
    private final Observable<Boolean> isShowLocationEnabled;

    @NonNull
    private final Observable<Object> shouldShowGpsAlert;

    @NonNull
    private final Observable<ZoomData> shouldShowZoomDialog;

    @NonNull
    private final Observable<LatLng> onMarkedLocation;

    @NonNull
    private final BehaviorRelay<Boolean> hasBeenCleared = BehaviorRelay.createDefault(false);

    // Internal state:

    @NonNull
    private final BehaviorRelay<Boolean> hasCurrentPosition = BehaviorRelay.createDefault(false);

    @NonNull
    private final Observable<Boolean> observeHasCurrentPosition = hasCurrentPosition.hide();

    @NonNull
    private final Observable<Boolean> hasInitialLocation;

    @NonNull
    private final Observable<Boolean> hasSelectedLocation;

    @NonNull
    private final PublishRelay<Object> showLocation = PublishRelay.create();

    @NonNull
    private final PublishRelay<Object> showLayers = PublishRelay.create();

    @NonNull
    private final PublishRelay<Object> clearLocation = PublishRelay.create();

    @NonNull
    private final PublishRelay<LatLng> shouldMarkLocation = PublishRelay.create();

    @NonNull
    private final Observable<Object> onShowLayers = showLayers.hide();

    @NonNull
    private final Observable<Object> onClearLocation = clearLocation.hide();

    @NonNull
    private final Observable<Boolean> isDraggable;

    @NonNull
    private final Observable<Boolean> isReadOnly;

    @Inject
    GeoViewModel(@NonNull InitialState initialState,
                 @NonNull WatchPosition watchPosition,
                 @NonNull SelectedLocation selectedLocation,
                 @NonNull InfoText infoText,
                 @NonNull StatusText statusText) {

        this.initialState = initialState;
        this.watchPosition = watchPosition;
        this.selectedLocation = selectedLocation;
        this.infoText = infoText;
        this.statusText = statusText;

        isDraggable = Observable.combineLatest(
                initialState.isDraggable(),
                initialState.isReadOnly(),
                (isDraggable, isReadOnly) ->
                        isDraggable && !isReadOnly
        );

        isReadOnly = initialState.isReadOnly();

        // Observe Location:
        hasInitialLocation = initialState.location()
                .map(Optional::isPresent)
                .distinctUntilChanged();

        hasSelectedLocation = selectedLocation.observe()
                .map(Optional::isPresent)
                .distinctUntilChanged();

        textVisibility = Observable.combineLatest(
                hasInitialLocation,
                hasBeenCleared.hide(),
                (hasInitial, wasCleared) ->
                        hasInitial && !wasCleared)
                .map(shouldHide -> shouldHide ? View.GONE : View.VISIBLE)
                .distinctUntilChanged();

        shouldShowGpsAlert = watchPosition.observeAvailability()
                .filter(isAvailable -> !isAvailable)
                .map(__ -> this);

        // Returns either the Initial location or the first location received from the GPS:
        Observable<Object> onFirstMarkedLocation = hasSelectedLocation
                .filter(Rx::isTrue)
                .distinctUntilChanged()
                .map(Rx::toEvent);

        Observable<Object> shouldZoomOnFirstLocation = onFirstMarkedLocation
                .withLatestFrom(hasInitialLocation, Rx::takeRight)
                .withLatestFrom(isDraggable, (hasInitialLocation, isDraggable) ->
                        hasInitialLocation || isDraggable)
                .filter(Rx::isFalse)
                .map(Rx::toEvent);

        shouldShowZoomDialog = Observable.merge(showLocation.hide(), shouldZoomOnFirstLocation)
                .flatMapSingle(__ -> Single.zip(
                        watchPosition.currentLocation(),
                        selectedLocation.get(),
                        (current, selected) -> new ZoomData(current.orNull(), selected.orNull())
                ));

        isShowLocationEnabled = Observable.combineLatest(observeHasCurrentPosition,
                hasSelectedLocation,
                Rx::or
        );

        Observable<LatLng> onFirstLocationNotInitial = observeHasCurrentPosition
                .filter(Rx::isTrue)
                .distinctUntilChanged()
                .flatMapSingle(__ -> watchPosition.currentLocation())
                .withLatestFrom(isDraggable, initialState.location(),
                        (location, draggable, initialLocation) ->
                                !draggable && !initialLocation.isPresent()
                                        ? location
                                        : Optional.<Location>absent()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::locationToLatLng);

        Observable<LatLng> onInitialLocation = initialState.location()
                .filter(Optional::isPresent)
                .map(Optional::get);

        @SuppressWarnings("unchecked")
        Observable<LatLng> shouldMarkInitialLocation = Observable.ambArray(
                onInitialLocation,
                onFirstLocationNotInitial
        );

        onMarkedLocation = Observable.merge(
                shouldMarkInitialLocation,
                shouldMarkLocation.hide()
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        initialState.set(bundle);

        watchPosition.observeLocation()
                .compose(bindToLifecycle())
                .map(Optional::isPresent)
                .subscribe(hasCurrentPosition, Timber::e);
    }

    @NonNull
    Observable<LatLng> observeAddLocation() {
        return onMarkedLocation;
    }

    @NonNull
    Observable<String> observeLocationInfoText() {
        return infoText;
    }

    @NonNull
    Observable<String> observeLocationStatusText() {
        return statusText;
    }

    @NonNull
    Observable<Integer> observeLocationInfoVisibility() {
        return textVisibility;
    }

    @NonNull
    Observable<Integer> observeLocationStatusVisibility() {
        return textVisibility;
    }

    @NonNull
    Observable<Integer> observePauseButtonVisibility() {
        return Observable.just(View.GONE);
    }

    @NonNull
    Observable<Boolean> observeAddLocationEnabled() {
        return Observable.combineLatest(
                isReadOnly,
                observeHasCurrentPosition,
                (isReadOnly, hasCurrentLocation) ->
                        !isReadOnly && hasCurrentLocation
        );
    }

    @NonNull
    Observable<Boolean> observeShowLocationEnabled() {
        return isShowLocationEnabled;
    }

    @NonNull
    Observable<Boolean> observeClearLocationEnabled() {
        return Observable.combineLatest(isReadOnly, hasInitialLocation, hasSelectedLocation,
                (isReadOnly, hasInitialLocation, hasSelectedLocation) ->
                        !isReadOnly && (hasInitialLocation || hasSelectedLocation)
        );
    }

    @NonNull
    Observable<Object> observeShowGpsAlert() {
        return shouldShowGpsAlert;
    }

    @NonNull
    Observable<ZoomData> observeShowZoomDialog() {
        return shouldShowZoomDialog;
    }

    @NonNull
    Observable<Object> observeShowLayers() {
        return onShowLayers;
    }

    @NonNull
    Observable<Object> observeLocationCleared() {
        return onClearLocation;
    }

    @NonNull
    Observable<Boolean> observeIsDraggable() {
        return isDraggable;
    }

    @NonNull
    Observable<Optional<LatLng>> observeInitialLocation() {
        return initialState.location()
                .doOnNext(__ -> Timber.i("initial."));
    }

    // Inputs:

    Completable addLocation() {
        return watchPosition.currentLocation()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::locationToLatLng)
                .doOnSuccess(shouldMarkLocation)
                .flatMapCompletable(__ -> Completable.complete());
    }

    Completable pause() {
        return Completable.complete();
    }

    Completable showLocation() {
        return Completable.defer(() -> {
            showLocation.accept(this);
            return Completable.complete();
        });
    }

    Completable showLayers() {
        return Completable.defer(() -> {
            showLayers.accept(this);
            return Completable.complete();
        });
    }

    Completable clearLocation() {
        return isReadOnly.flatMapCompletable(isReadOnly -> {
            if (!isReadOnly) {
                clearLocation.accept(this);
            }

            return Completable.complete();
        });
    }

    Completable selectLocation(@NonNull LatLng latLng) {
        return Completable.defer(() -> {
            selectedLocation.update(latLng);
            return Completable.complete();
        });
    }

    Completable clearSelectedLocation() {
        return Completable.defer(() -> {
            selectedLocation.clear();
            hasBeenCleared.accept(true);
            return Completable.complete();
        });
    }

    Single<String> saveLocation() {
        return Single.just("");
    }

    void startWatchingLocation() {
        watchPosition.startWatching();
    }

    void stopWatchingLocation() {
        watchPosition.stopWatching();
    }

    @NonNull
    private LatLng locationToLatLng(@NonNull Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
