using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    /// <summary>Small modular UI props for the existing code-native prototype world.</summary>
    internal static partial class CompanionWorldArt
    {
        public static RectTransform Shape(Transform parent, string name, Vector2 anchor, Vector2 size, Color color)
        {
            var go = new GameObject(name, typeof(RectTransform), typeof(CanvasRenderer), typeof(Image));
            var rect = (RectTransform)go.transform;
            rect.SetParent(parent,false); rect.anchorMin = rect.anchorMax = anchor;
            rect.sizeDelta = size; rect.anchoredPosition = Vector2.zero;
            var image = go.GetComponent<Image>(); image.color = color; image.raycastTarget = false;
            return rect;
        }
        public static GameObject Decoration(Transform parent, int id)
        {
            var root = new GameObject("Decoration" + id, typeof(RectTransform));
            var rect = (RectTransform)root.transform; rect.SetParent(parent,false);
            rect.anchorMin = rect.anchorMax = new Vector2(.5f,.5f); rect.sizeDelta = new Vector2(100,80);
            var wood = new Color(.56f,.37f,.22f); var leaf = new Color(.37f,.65f,.4f); var cream = new Color(.96f,.9f,.72f);
            if (id == 0)
            {
                Shape(rect,"Shelf",new Vector2(.5f,.12f),new Vector2(100,12),wood);
                for(int i=0;i<4;i++) Shape(rect,"Book"+i,new Vector2(.2f+i*.2f,.5f),new Vector2(14,50+i%2*12),i%2==0?leaf:cream);
            }
            else if (id == 1)
            {
                Shape(rect,"Toolbox",new Vector2(.5f,.3f),new Vector2(90,42),wood);
                Shape(rect,"Handle",new Vector2(.5f,.65f),new Vector2(45,10),cream);
                Shape(rect,"Tool",new Vector2(.75f,.62f),new Vector2(9,35),leaf);
            }
            else if (id == 2)
            {
                Shape(rect,"CupHandle",new Vector2(.85f,.52f),new Vector2(28,28),cream);
                Shape(rect,"Cup",new Vector2(.48f,.45f),new Vector2(62,40),cream);
                Shape(rect,"Tea",new Vector2(.48f,.71f),new Vector2(58,7),wood);
                Shape(rect,"Flower",new Vector2(.48f,.45f),new Vector2(13,13),new Color(.85f,.53f,.49f));
            }
            else if (id == 3)
            {
                Shape(rect,"Pot",new Vector2(.5f,.25f),new Vector2(46,36),wood);
                Shape(rect,"Stem",new Vector2(.5f,.65f),new Vector2(5,40),leaf);
                Shape(rect,"LeafLeft",new Vector2(.34f,.72f),new Vector2(25,13),leaf).localRotation=Quaternion.Euler(0,0,-30);
                Shape(rect,"LeafRight",new Vector2(.66f,.85f),new Vector2(25,13),leaf).localRotation=Quaternion.Euler(0,0,30);
            }
            else
            {
                Shape(rect,"Cushion",new Vector2(.5f,.4f),new Vector2(100,43),new Color(.88f,.7f,.61f));
                Shape(rect,"CushionSeam",new Vector2(.5f,.4f),new Vector2(70,3),cream);
            }
            return root;
        }
    }
}
