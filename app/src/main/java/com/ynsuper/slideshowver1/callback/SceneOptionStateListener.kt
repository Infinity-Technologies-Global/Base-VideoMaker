package com.ynsuper.slideshowver1.callback

import com.ynsuper.slideshowver1.bottomsheet.BackgroundOptionsBottomSheet
import com.ynsuper.slideshowver1.bottomsheet.DurationOptionsBottomSheet

interface SceneOptionStateListener {
    fun onDurationChange(state: DurationOptionsBottomSheet.OptionState)

    fun onBackGroundConfigChange(state: BackgroundOptionsBottomSheet.OptionState)
}