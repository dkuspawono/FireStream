//
//  PopUp.swift
//  MyGizmoUi
//
//  Created by Caleb Adcock on 11/15/16.
//  Copyright © 2016 Trayak, LLC. All rights reserved.
//

import UIKit

class PopUp: UIView {
	@IBOutlet weak var viewContainer: UIView!
	@IBOutlet weak var viewPopUp: UIView!
	@IBOutlet weak var constraintBottom: NSLayoutConstraint!
	
	@IBAction func close() {
		removeFromSuperview()
	}
	
	override func awakeFromNib() {
		super.awakeFromNib()
		
		viewContainer.layer.shadowColor = UIColor.black.cgColor
		viewContainer.layer.shadowOffset = CGSize(width: 0, height: 10)
		viewContainer.layer.shadowOpacity = 0.6
		viewContainer.layer.shadowRadius = 10
	}
	
	override func didMoveToSuperview() {
		super.didMoveToSuperview()
		
		viewContainer.isHidden = true
		let finalX = viewPopUp.frame.origin.x
		let finalY = viewPopUp.frame.origin.y
		let finalWidth = viewPopUp.frame.width
		let finalHeight = viewPopUp.frame.height
		
		viewPopUp.frame = CGRect(x: viewPopUp.center.x,y: viewPopUp.center.y,width: 0,height: 0)
		
		UIView.animate(withDuration: 0.4, delay: 0.0, usingSpringWithDamping: 0.65, initialSpringVelocity: 10, options: UIViewAnimationOptions(), animations: {
			self.viewPopUp.frame = CGRect(x: finalX,y: finalY,width: finalWidth, height: finalHeight)
		}, completion:{ (Bool)  in
			self.viewContainer.isHidden = false
			let shadowPath = UIBezierPath(rect: self.viewPopUp.bounds)
			self.viewContainer.layer.shadowPath = shadowPath.cgPath
		})
	}
}
