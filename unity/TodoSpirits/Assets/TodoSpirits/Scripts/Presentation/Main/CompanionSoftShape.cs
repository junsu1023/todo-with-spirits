using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    /// <summary>Resolution-independent soft silhouettes for the prototype's modular UI art.</summary>
    public sealed class CompanionSoftShape : MaskableGraphic
    {
        [Range(0f, 1f)] public float Roundness = 1f;

        protected override void OnPopulateMesh(VertexHelper mesh)
        {
            mesh.Clear();
            var rect = rectTransform.rect;
            var center = rect.center;
            const int segments = 48;
            mesh.AddVert(center, color, new Vector2(.5f, .5f));
            float exponent = Mathf.Lerp(.25f, 1f, Roundness);
            for (int i = 0; i < segments; i++)
            {
                float angle = i * Mathf.PI * 2 / segments;
                float x = Mathf.Cos(angle), y = Mathf.Sin(angle);
                x = Mathf.Sign(x) * Mathf.Pow(Mathf.Abs(x), exponent);
                y = Mathf.Sign(y) * Mathf.Pow(Mathf.Abs(y), exponent);
                mesh.AddVert(center + new Vector2(x * rect.width * .5f, y * rect.height * .5f), color, new Vector2((x + 1) * .5f, (y + 1) * .5f));
            }
            for (int i = 0; i < segments; i++) mesh.AddTriangle(0, i + 1, (i + 1) % segments + 1);
        }
    }
}
