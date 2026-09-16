using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Main
{
    internal static partial class CompanionWorldArt
    {
        private static readonly Color Bark = new Color(.48f,.36f,.27f);
        private static readonly Color Timber = new Color(.73f,.57f,.38f);
        private static readonly Color Paper = new Color(.98f,.94f,.81f);
        private static readonly Color Moss = new Color(.39f,.56f,.37f);
        private static readonly Color Leaf = new Color(.62f,.74f,.48f);
        private static readonly Color Peach = new Color(.87f,.65f,.56f);

        public static RectTransform Soft(Transform parent, string name, Vector2 anchor, Vector2 size, Color color, float roundness = 1)
        {
            var go = new GameObject(name, typeof(RectTransform), typeof(CanvasRenderer), typeof(CompanionSoftShape));
            var rect = (RectTransform)go.transform;
            rect.SetParent(parent, false); rect.anchorMin = rect.anchorMax = anchor;
            rect.sizeDelta = size;
            var graphic = go.GetComponent<CompanionSoftShape>();
            graphic.Roundness = roundness; graphic.color = color; graphic.raycastTarget = false;
            return rect;
        }

        private static RectTransform Stretch(Transform parent, string name, Vector2 min, Vector2 max, Color color, float roundness = .2f)
        {
            var rect = Soft(parent,name,Vector2.zero,Vector2.zero,color,roundness);
            rect.anchorMin = min; rect.anchorMax = max; rect.offsetMin = rect.offsetMax = Vector2.zero;
            return rect;
        }

        public static void BuildLivingSpace(RectTransform world, IReadOnlyList<ActivitySpotBinding> spots)
        {
            var stage = Stretch(world,"LivingSpaceArt",new Vector2(.025f,.24f),new Vector2(.975f,.88f),new Color(.8f,.86f,.7f));
            stage.SetAsFirstSibling();
            Stretch(stage,"GardenLawn",new Vector2(.01f,.01f),new Vector2(.99f,.57f),new Color(.75f,.83f,.62f),1);
            Stretch(stage,"HouseShadow",new Vector2(.03f,.43f),new Vector2(.99f,.95f),new Color(.38f,.48f,.31f,.16f));
            Stretch(stage,"HouseWall",new Vector2(.04f,.58f),new Vector2(.96f,.98f),new Color(.93f,.88f,.72f));
            Stretch(stage,"HouseFloor",new Vector2(.04f,.43f),new Vector2(.96f,.68f),new Color(.84f,.72f,.53f));
            for(int i=0;i<5;i++)
                Stretch(stage,"FloorSeam"+i,new Vector2(.06f,.45f+i*.043f),new Vector2(.94f,.454f+i*.043f),new Color(.65f,.49f,.32f,.3f));
            Stretch(stage,"LeftBeam",new Vector2(.03f,.48f),new Vector2(.06f,.98f),Timber);
            Stretch(stage,"RightBeam",new Vector2(.94f,.48f),new Vector2(.97f,.98f),Timber);
            Stretch(stage,"RoofBeam",new Vector2(.015f,.95f),new Vector2(.985f,1),Bark);
            var window = Stretch(stage,"WindowFrame",new Vector2(.4f,.74f),new Vector2(.6f,.94f),Timber);
            Stretch(window,"WindowSky",new Vector2(.1f,.1f),new Vector2(.9f,.9f),new Color(.73f,.85f,.85f));
            Stretch(window,"WindowCrossVertical",new Vector2(.47f,.08f),new Vector2(.53f,.92f),Paper);
            Stretch(window,"WindowCrossHorizontal",new Vector2(.08f,.46f),new Vector2(.92f,.52f),Paper);
            for(int i=0;i<6;i++)
                Soft(stage,"PathStone"+i,new Vector2(.28f+i*.064f,.14f+i*.047f),new Vector2(49,21),new Color(.9f,.86f,.73f));
            for(int side=0;side<2;side++)
            {
                float x=side==0?.04f:.96f;
                Soft(stage,"BushShadow"+side,new Vector2(x,.48f),new Vector2(88,115),Moss);
                Soft(stage,"BushLight"+side,new Vector2(x-.012f,.53f),new Vector2(76,86),Leaf);
                for(int i=0;i<3;i++) Soft(stage,"Flower"+side+"-"+i,new Vector2(x+(i-1)*.025f,.45f+i%2*.05f),new Vector2(13,13),Paper);
            }
            foreach(var spot in spots) BuildFurniture(spot);
        }

        private static void BuildFurniture(ActivitySpotBinding spot)
        {
            var parent = spot.Destination.parent;
            var oldBackground = parent.GetComponent<Image>();
            if(oldBackground!=null) oldBackground.enabled = false;
            // Retain the authored labels and anchors; only the temporary prop word is hidden.
            var propLabel = parent.Find("PropLabel");
            if(propLabel!=null) propLabel.gameObject.SetActive(false);
            var root = Stretch(parent,"FurnitureArt",new Vector2(0,.2f),new Vector2(1,1),Color.clear);
            root.SetAsFirstSibling();
            Soft(root,"GroundShadow",new Vector2(.5f,.16f),new Vector2(190,28),new Color(.24f,.34f,.22f,.16f));
            if(spot.StableId==ActivitySpotId.Desk || spot.StableId==ActivitySpotId.Workshop)
            {
                Shape(root,"LeftLeg",new Vector2(.18f,.35f),new Vector2(14,47),Bark);
                Shape(root,"RightLeg",new Vector2(.82f,.35f),new Vector2(14,47),Bark);
                Soft(root,"TableTop",new Vector2(.5f,.63f),new Vector2(192,45),Timber,.15f);
                if(spot.StableId==ActivitySpotId.Desk)
                {
                    Soft(root,"OpenBook",new Vector2(.43f,.81f),new Vector2(74,35),Paper,.1f);
                    Shape(root,"BookSpine",new Vector2(.43f,.81f),new Vector2(3,29),Timber);
                    Shape(root,"Pencil",new Vector2(.75f,.77f),new Vector2(42,5),Moss).localRotation=Quaternion.Euler(0,0,-20);
                }
                else
                {
                    Soft(root,"WoodBlock",new Vector2(.38f,.85f),new Vector2(53,29),Paper,.15f);
                    Shape(root,"HammerHandle",new Vector2(.7f,.82f),new Vector2(9,39),Bark).localRotation=Quaternion.Euler(0,0,-30);
                    Soft(root,"HammerHead",new Vector2(.66f,.99f),new Vector2(32,16),Moss,.2f);
                }
            }
            else if(spot.StableId==ActivitySpotId.TeaTable)
            {
                Shape(root,"TeaTableStem",new Vector2(.5f,.42f),new Vector2(17,54),Bark);
                Soft(root,"TeaTableTop",new Vector2(.5f,.7f),new Vector2(170,54),Timber);
                for(int i=0;i<2;i++)
                {
                    float x=.3f+i*.4f;
                    Soft(root,"CupHandle"+i,new Vector2(x+.09f,.83f),new Vector2(16,16),Paper);
                    Soft(root,"Cup"+i,new Vector2(x,.83f),new Vector2(32,24),Paper,.3f);
                    Soft(root,"Tea"+i,new Vector2(x,.91f),new Vector2(24,6),Bark);
                }
            }
            else if(spot.StableId==ActivitySpotId.Rest)
            {
                Soft(root,"BedFrame",new Vector2(.5f,.48f),new Vector2(190,72),Timber,.25f);
                Soft(root,"BedLinen",new Vector2(.5f,.6f),new Vector2(172,58),Paper,.25f);
                Soft(root,"Blanket",new Vector2(.61f,.6f),new Vector2(112,58),Peach,.25f);
                Soft(root,"Pillow",new Vector2(.2f,.63f),new Vector2(40,43),new Color(.99f,.97f,.9f),.3f);
            }
            else
            {
                for(int i=0;i<3;i++) Soft(root,"GardenStone"+i,new Vector2(.3f+i*.2f,.3f+i*.15f),new Vector2(57,24),Paper);
                Shape(root,"SignPost",new Vector2(.83f,.56f),new Vector2(10,60),Bark);
                Soft(root,"TrailSign",new Vector2(.83f,.84f),new Vector2(58,27),Timber,.2f);
                Shape(root,"TrailArrow",new Vector2(.83f,.84f),new Vector2(26,4),Paper);
                Soft(root,"Grass",new Vector2(.12f,.39f),new Vector2(38,56),Leaf);
            }
        }
    }
}
