package com.example.todowithspirits.feature.bestiary.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.SpiritCollectionItem
import com.example.todowithspirits.R
import com.example.todowithspirits.theme.SpiritTodoTheme

private const val MaxAffectionLevel = 5
private val CardShape = RoundedCornerShape(8.dp)

// 미발견 정령은 이미지를 흑백으로 흐리게 보여준다.
private val UndiscoveredColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
private const val UndiscoveredImageAlpha = 0.3f

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
                    if (spirit.isOwned) {
                        Modifier
                            .background(SpiritTodoTheme.color.surfaceColor1, CardShape)
                            .border(1.dp, SpiritTodoTheme.color.temporaryColor1, CardShape)
                    } else {
                        Modifier.background(SpiritTodoTheme.color.surfaceColor17, CardShape)
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

            if (spirit.isRepresentative) {
                RepresentativeBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                )
            }
        }

        if (spirit.isOwned) {
            Text(
                text = spirit.spiritName ?: spirit.speciesName,
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.todoTextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            AffectionHearts(level = spirit.affectionLevel)
        } else {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.undiscovered_spirit_name),
                fontSize = 12.sp,
                color = SpiritTodoTheme.color.systemGrey
            )

            Text(
                text = stringResource(R.string.undiscovered_spirit),
                fontSize = 10.sp,
                color = SpiritTodoTheme.color.systemGrey
            )
        }
    }
}

@Composable
private fun RepresentativeBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(18.dp)
            .background(SpiritTodoTheme.color.mainBackground, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.representative),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SpiritTodoTheme.color.mainTextAndStroke
        )
    }
}

// 호감도 단계만큼 채운 하트 5개
@Composable
private fun AffectionHearts(level: Int) {
    Row {
        repeat(MaxAffectionLevel) { index ->
            Image(
                painter = painterResource(
                    if (index < level) R.drawable.ic_affection_heart_on else R.drawable.ic_affection_heart_off
                ),
                contentDescription = null
            )
        }
    }
}
