using System;
using UnityEngine;

namespace TodoSpirits.Spirit
{
    // Three small shapes cover the neutral smile and draw the open mouth while dragging.
    internal sealed class SpiritExpression : IDisposable
    {
        private readonly GameObject expression;
        private readonly Texture2D texture;
        private readonly Sprite circle;

        public SpiritExpression(SpriteRenderer body)
        {
            expression = new GameObject("Drag expression");
            expression.transform.SetParent(body.transform, false);

            const int size = 32;
            texture = new Texture2D(size, size, TextureFormat.RGBA32, false);
            var pixels = new Color32[size * size];
            for (var y = 0; y < size; y++)
            for (var x = 0; x < size; x++)
            {
                var offset = new Vector2(x + .5f, y + .5f) - Vector2.one * (size / 2f);
                pixels[y * size + x] = new Color(1, 1, 1, Mathf.Clamp01(size / 2f - offset.magnitude));
            }
            texture.SetPixels32(pixels);
            texture.Apply(false, true);
            circle = Sprite.Create(texture, new Rect(0, 0, size, size), new Vector2(.5f, .5f), size);

            var sprite = body.sprite;
            var face = sprite.texture.GetPixel(
                Mathf.RoundToInt(sprite.rect.x + sprite.rect.width * .535f),
                Mathf.RoundToInt(sprite.rect.y + sprite.rect.height * .568f));
            AddShape("Cheek color", .490f, .568f, .075f, .046f, face, body, 1);
            AddShape("Open mouth", .490f, .568f, .027f, .032f, new Color(.35f, .17f, .28f), body, 2);
            AddShape("Mouth highlight", .490f, .563f, .015f, .012f, new Color(.87f, .44f, .61f), body, 3);
            SetDragging(false);
        }

        private void AddShape(string label, float x, float y, float width, float height, Color color, SpriteRenderer body, int layer)
        {
            var shape = new GameObject(label);
            shape.transform.SetParent(expression.transform, false);
            var sprite = body.sprite;
            shape.transform.localPosition = new Vector3(
                (sprite.rect.width * x - sprite.pivot.x) / sprite.pixelsPerUnit,
                (sprite.rect.height * y - sprite.pivot.y) / sprite.pixelsPerUnit, 0);
            shape.transform.localScale = new Vector3(sprite.bounds.size.x * width, sprite.bounds.size.y * height, 1);
            var renderer = shape.AddComponent<SpriteRenderer>();
            renderer.sprite = circle;
            renderer.color = color;
            renderer.sharedMaterial = body.sharedMaterial;
            renderer.sortingLayerID = body.sortingLayerID;
            renderer.sortingOrder = body.sortingOrder + layer;
        }

        public void SetDragging(bool dragging)
        {
            if (expression.activeSelf != dragging)
                expression.SetActive(dragging);
        }

        public void Dispose()
        {
            UnityEngine.Object.Destroy(expression);
            UnityEngine.Object.Destroy(circle);
            UnityEngine.Object.Destroy(texture);
        }
    }
}
