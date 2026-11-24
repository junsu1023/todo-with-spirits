package com.example.todowithspirits.feature.bestiary.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.SpiritCollectionItem
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

private const val MaxAffectionLevel = 5
private val CardShape = RoundedCornerShape(8.dp)
private val RepresentativeInnerShape = RoundedCornerShape(6.dp)

// 미발견 정령은 이미지를 흑백으로 흐리게 보여준다.
private val UndiscoveredColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
private const val UndiscoveredImageAlpha = 0.3f

// 대표 정령은 베이지 바탕 안에 흰 칸을 한 겹 더 둘러 강조한다.
@Composable
fun BestiarySpiritCard(
    spirit: SpiritCollectionItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(
                    if (spirit.isRepresentative) {
                        Modifier
                            .background(SpiritTodoTheme.color.back03, CardShape)
                            .border(1.dp, SpiritTodoTheme.color.font02, CardShape)
                            .padding(3.dp)
                            .background(SpiritTodoTheme.color.white, RepresentativeInnerShape)
                            .border(1.dp, SpiritTodoTheme.color.font02, RepresentativeInnerShape)
                    } else {
                        Modifier
                            .background(SpiritTodoTheme.color.white, CardShape)
                            .border(1.dp, SpiritTodoTheme.color.temporaryColor1, CardShape)
                    }
                )
        ) {
            AsyncImage(
                model = spirit.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentScale = ContentScale.Fit,
                alpha = if (spirit.isOwned) 1f else UndiscoveredImageAlpha,
                colorFilter = if (spirit.isOwned) null else UndiscoveredColorFilter
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (spirit.isOwned) {
                spirit.spiritName ?: spirit.speciesName
            } else {
                stringResource(R.string.undiscovered_spirit_name)
            },
            fontSize = 12.sp,
            color = SpiritTodoTheme.color.font01,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        AffectionHearts(level = spirit.affectionLevel)
    }
}

// 호감도 단계만큼 채운 하트 5개. 미보유는 단계가 0이라 모두 빈 하트다.
@Composable
private fun AffectionHearts(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(MaxAffectionLevel) { index ->
            Image(
                painter = painterResource(
                    if (index < level) R.drawable.widgetly_heart_on else R.drawable.widgetly_heart_off
                ),
                contentDescription = null
            )
        }
    }
}
