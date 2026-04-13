package org.example.project

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.IntOffset
import kotlin.math.max

private const val PAGE_TRANSITION_DURATION_MILLIS = 300
private val PageTransitionEasing = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)

@Composable
internal fun <T> FitBoardPageTransition(
    targetState: T,
    depth: (T) -> Int,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
        transitionSpec = {
            when {
                depth(targetState) > depth(initialState) -> forwardPageTransition()
                depth(targetState) < depth(initialState) -> backwardPageTransition()
                else -> lateralPageTransition()
            }
        },
        label = "fitBoardPageTransition"
    ) { state ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            FitBoardColors.bgGradientStart,
                            FitBoardColors.bgGradientEnd
                        )
                    )
                )
        ) {
            content(state)
        }
    }
}

private fun <T> AnimatedContentTransitionScope<T>.forwardPageTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = slideInHorizontally(
            animationSpec = tween<IntOffset>(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            initialOffsetX = { fullWidth -> max(fullWidth / 4, 1) }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            initialAlpha = 0.92f
        ),
        initialContentExit = slideOutHorizontally(
            animationSpec = tween<IntOffset>(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            targetOffsetX = { fullWidth -> -max(fullWidth / 10, 1) }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            targetAlpha = 0.9f
        ),
        targetContentZIndex = 1f,
        sizeTransform = SizeTransform(clip = false)
    )
}

private fun <T> AnimatedContentTransitionScope<T>.backwardPageTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = slideInHorizontally(
            animationSpec = tween<IntOffset>(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            initialOffsetX = { fullWidth -> -max(fullWidth / 10, 1) }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            initialAlpha = 0.94f
        ),
        initialContentExit = slideOutHorizontally(
            animationSpec = tween<IntOffset>(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            targetOffsetX = { fullWidth -> max(fullWidth / 4, 1) }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            targetAlpha = 0.9f
        ),
        targetContentZIndex = 1f,
        sizeTransform = SizeTransform(clip = false)
    )
}

private fun <T> AnimatedContentTransitionScope<T>.lateralPageTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = fadeIn(
            animationSpec = tween(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            initialAlpha = 0.96f
        ),
        initialContentExit = fadeOut(
            animationSpec = tween(
                durationMillis = PAGE_TRANSITION_DURATION_MILLIS,
                easing = PageTransitionEasing
            ),
            targetAlpha = 0.96f
        ),
        targetContentZIndex = 1f,
        sizeTransform = SizeTransform(clip = false)
    )
}
